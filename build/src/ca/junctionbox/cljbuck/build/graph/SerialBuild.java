package ca.junctionbox.cljbuck.build.graph;

import ca.junctionbox.cljbuck.build.rules.BuildRule;

import java.util.ArrayList;

public class SerialBuild implements Walken {
    private ArrayList<BuildRule> rules;

    public SerialBuild() {
        this.rules = new ArrayList<>();
    }

    @Override
    public void step(final BuildRule buildRule, final int depth) {
        rules.add(buildRule);
    }

    public void build() {
        for (int i = rules.size() - 1; i >= 0; i--) {
            final BuildRule rule = rules.get(i);
            rule.prepare();
            rule.build();
        }
    }
}
