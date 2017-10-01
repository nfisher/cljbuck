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
            rules.get(i).build();
        }
    }

    public void prepare() {
        for (int i = rules.size() - 1; i >= 0; i--) {
            rules.get(i).prepare();
        }
    }
}
