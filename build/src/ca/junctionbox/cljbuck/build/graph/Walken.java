package ca.junctionbox.cljbuck.build.graph;

import ca.junctionbox.cljbuck.build.rules.BuildRule;

public interface Walken {
    void step(final BuildRule buildRule, final int depth);
}
