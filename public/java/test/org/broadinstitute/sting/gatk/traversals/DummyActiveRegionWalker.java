/*
* Copyright (c) 2012 The Broad Institute
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.sting.gatk.traversals;

import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.ActiveRegionWalker;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.GenomeLocSortedSet;
import org.broadinstitute.sting.utils.activeregion.ActiveRegion;
import org.broadinstitute.sting.utils.activeregion.ActiveRegionReadState;
import org.broadinstitute.sting.utils.activeregion.ActivityProfileState;

import java.util.*;

/**
 * ActiveRegionWalker for unit testing
 *
 * User: depristo
 * Date: 1/15/13
 * Time: 1:28 PM
 */
class DummyActiveRegionWalker extends ActiveRegionWalker<Integer, Integer> {
    private final double prob;
    private EnumSet<ActiveRegionReadState> states = super.desiredReadStates();
    private GenomeLocSortedSet activeRegions = null;

    protected List<GenomeLoc> isActiveCalls = new ArrayList<GenomeLoc>();
    protected Map<GenomeLoc, ActiveRegion> mappedActiveRegions = new LinkedHashMap<GenomeLoc, ActiveRegion>();

    public DummyActiveRegionWalker() {
        this(1.0);
    }

    public DummyActiveRegionWalker(double constProb) {
        this.prob = constProb;
    }

    public DummyActiveRegionWalker(EnumSet<ActiveRegionReadState> wantStates) {
        this(1.0);
        this.states = wantStates;
    }

    public DummyActiveRegionWalker(GenomeLocSortedSet activeRegions) {
        this(1.0);
        this.activeRegions = activeRegions;
    }

    public void setStates(EnumSet<ActiveRegionReadState> states) {
        this.states = states;
    }

    @Override
    public EnumSet<ActiveRegionReadState> desiredReadStates() {
        return states;
    }

    @Override
    public ActivityProfileState isActive(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        isActiveCalls.add(ref.getLocus());
        final double p = activeRegions == null || activeRegions.overlaps(ref.getLocus()) ? prob : 0.0;
        return new ActivityProfileState(ref.getLocus(), p);
    }

    @Override
    public Integer map(ActiveRegion activeRegion, RefMetaDataTracker metaDataTracker) {
        mappedActiveRegions.put(activeRegion.getLocation(), activeRegion);
        return 0;
    }

    @Override
    public Integer reduceInit() {
        return 0;
    }

    @Override
    public Integer reduce(Integer value, Integer sum) {
        return 0;
    }
}
