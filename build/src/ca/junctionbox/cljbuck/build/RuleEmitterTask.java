package ca.junctionbox.cljbuck.build;

import ca.junctionbox.cljbuck.build.json.Tracer;
import ca.junctionbox.cljbuck.build.rules.BuildRule;
import ca.junctionbox.cljbuck.channel.Closer;
import ca.junctionbox.cljbuck.channel.Reader;
import ca.junctionbox.cljbuck.channel.Writer;
import ca.junctionbox.cljbuck.lexer.Item;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static ca.junctionbox.cljbuck.build.Rules.cljBinary;
import static ca.junctionbox.cljbuck.build.Rules.cljLib;
import static ca.junctionbox.cljbuck.build.Rules.cljTest;
import static ca.junctionbox.cljbuck.build.Rules.jar;
import static ca.junctionbox.cljbuck.build.json.Event.finished;
import static ca.junctionbox.cljbuck.build.json.Event.started;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemEOF;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemKeyword;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemLeftParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemRightParen;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemShutdown;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemString;
import static ca.junctionbox.cljbuck.lexer.ItemType.itemSymbol;

class RuleEmitterTask implements Callable<Integer> {
    private final Reader in;
    private final Writer out;
    private final Tracer tracer;
    private final Workspace workspace;
    private int countDown;

    class Rule {
        Rules rule;
        String key;
    }

    public RuleEmitterTask(final Tracer tracer, final Reader in, final Writer out, final Workspace workspace, final int lexerTasks) {
        this.tracer = tracer;
        this.in = in;
        this.out = out;
        this.countDown = lexerTasks;
        this.workspace = workspace;
    }

    @Override
    public Integer call() throws Exception {
        tracer.info(started(hashCode()).toString());
        // force the class loader to load the graph classes early here
        final MutableGraph<BuildRule> graph = GraphBuilder.directed().expectedNodeCount(10).build();
        final ConcurrentHashMap<String, Rule> map = new ConcurrentHashMap<>();

        for (;;) {
            final Object o = in.read();

            if (o instanceof Closer) {
                countDown--;
                if (countDown == 0) {
                    break;
                }
                continue;
            }

            final Item token = (Item) o;
            final Rule rule = (map.containsKey(token.filename))
                    ? map.get(token.filename)
                    : new Rule();

            // TODO: consider stripping /CLJ from filename as it's only really needed for fopen.
            map.put(token.filename, rule);

            if (itemShutdown == token.type) {
                break;
            } else if (itemLeftParen == token.type || itemEOF == token.type) {
                // ignore it for now...
            } else if (itemRightParen == token.type) {
                out.write(rule.rule);
                rule.rule = null;
                rule.key = null; // e.g. :name
            } else if (itemSymbol == token.type) {
                switch (token.val) {
                    case "jar":
                        rule.rule = jar();
                        break;

                    case "clj-test":
                        rule.rule = cljTest();
                        break;

                    case "clj-binary":
                        rule.rule = cljBinary();
                        break;

                    case "clj-lib":
                        rule.rule = cljLib();
                        break;
                }
            } else if (itemKeyword == token.type) {
                rule.key = token.val;
            } else if (itemString == token.type) {
                switch (rule.key) {
                    case ":name":
                        final String targetname = workspace.workspaceRelative(token.filename, token.val);
                        rule.rule = rule.rule.name(targetname);
                        break;

                    case ":jar":
                        final String jar = workspace.workspaceAbsolute(token.filename, token.val);
                        rule.rule = rule.rule.binaryJar(jar);
                        break;

                    case ":deps":
                        // if it's a dep like :lib expand to a workspace relative path
                        if (token.val.startsWith(":")) {
                            final String depname = workspace.workspaceRelative(token.filename, token.val.substring(1, token.val.length()));
                            rule.rule = rule.rule.appendDep(depname);
                            break;
                        }
                        rule.rule = rule.rule.appendDep(token.val);
                        break;

                    case ":visibility":
                        rule.rule = rule.rule.appendVisibility(token.val);
                        break;

                    case ":srcs":
                        final String src = workspace.workspaceAbsolute(token.filename, token.val);
                        rule.rule = rule.rule.appendSrc(src);
                        break;

                    case ":main":
                        rule.rule = rule.rule.main(token.val);
                        break;

                    case ":ns":
                        rule.rule = rule.rule.ns(token.val);
                        break;

                    default:
                        return -1;
                }
            }
        }

        tracer.info(finished(hashCode()).toString());
        return 0;
    }
}
