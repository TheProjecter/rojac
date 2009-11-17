package org.xblackcat.rojac.service.commands;

import ch.lambdaj.function.aggregate.Aggregator;
import gnu.trove.TIntHashSet;

/**
* @author xBlackCat
*/
class AffectedPostsAggregator implements Aggregator<AffectedIds> {
    @Override
    public AffectedIds aggregate(Iterable<? extends AffectedIds> iterable) {
        TIntHashSet mIds = new TIntHashSet();
        TIntHashSet fIds = new TIntHashSet();

        for (AffectedIds p : iterable) {
            mIds.addAll(p.getMessageIds());
            fIds.addAll(p.getForumIds());
    }

        return new AffectedIds(mIds, fIds);
    }
}
