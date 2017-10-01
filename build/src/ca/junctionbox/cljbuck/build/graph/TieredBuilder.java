package ca.junctionbox.cljbuck.build.graph;

import ca.junctionbox.cljbuck.build.rules.BuildRule;

import java.util.ArrayList;
import java.util.List;

public class TieredBuilder implements Walken {
    final ArrayList<List<BuildRule>> tiers;

    public TieredBuilder(final ArrayList<List<BuildRule>> tiers) {
        this.tiers = tiers;
    }

    @Override
    public void step(final BuildRule buildRule, int depth) {
        this.tiers.size();
    }
}
