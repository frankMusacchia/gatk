package org.broadinstitute.sting.oneoffprojects.walkers;

import net.sf.samtools.util.CloseableIterator;
import org.broad.tribble.FeatureIterator;
import org.broad.tribble.FeatureReader;
import org.broad.tribble.dbsnp.DbSNPCodec;
import org.broad.tribble.dbsnp.DbSNPFeature;
import org.broad.tribble.util.CloseableTribbleIterator;
import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.refdata.tracks.builders.TribbleRMDTrackBuilder;
import org.broadinstitute.sting.gatk.walkers.By;
import org.broadinstitute.sting.gatk.walkers.DataSource;
import org.broadinstitute.sting.gatk.walkers.LocusWalker;
import org.broadinstitute.sting.gatk.walkers.Requires;
import org.broadinstitute.sting.utils.StingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * DbSNPWindowCounter
 *
 * Count the number of upstream and downstream dbSNP entries from the current position using the specified window size.
 * (really the window size upstream and downstream, so windowSize * 2)
 *
 * @Author Aaron
 * @Date May 7th, 2010
 */
@By(DataSource.REFERENCE)
@Requires({DataSource.REFERENCE, DataSource.REFERENCE_BASES})
public class DbSNPWindowCounter extends LocusWalker<Integer, Long> {

    // what we read in new tracks with
    private FeatureReader reader;

    @Argument(fullName = "dbSNPFile", shortName = "db", doc="The dbsnp file to search upstream and downstream for nearby snps", required = true)
    private File myDbSNPFile;

    @Argument(fullName = "dbSNPWindowSize", shortName = "dbw", doc="The distance to look both upstream and downstream for SNPs", required = true)
    private int windowSize;


    public void initialize() {
        TribbleRMDTrackBuilder builder = new TribbleRMDTrackBuilder();
        reader = builder.createFeatureReader(DbSNPCodec.class,myDbSNPFile);
    }


    public Integer map(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        CloseableTribbleIterator<DbSNPFeature> dbSNPs;

        // our upstream and downstream window locations
        int windowStart = (int)Math.max(context.getLocation().getStart()-windowSize,0);
        int windowStop  = (int)context.getLocation().getStop()+windowSize;

        // query the dnSNP iterator
        try {
            dbSNPs = reader.query(context.getContig(),
                    windowStart,
                    windowStop);
        } catch (IOException e) {
            throw new StingException("Unable to query dbSNP track due to IO Exception",e);
        }

        // count the number of dbSNPs we've seen
        int counter = 0;
        for (DbSNPFeature feature: dbSNPs)
            counter++;
        out.println(context.getContig() + ":" + windowStart + "-" + context.getContig() + ":" + windowStop + "=" +
                    counter + " (dnSNP records)");
        return 1;
    }

    public Long reduceInit() { return 0l; }

    public Long reduce(Integer value, Long sum) {
        return value + sum;
    }
}