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

    public int size() {
        return rules.size();
    }

    public BuildRule get(final int i) {
        final int pos = rules.size() - i - 1;
        return rules.get(pos);
    }
}
