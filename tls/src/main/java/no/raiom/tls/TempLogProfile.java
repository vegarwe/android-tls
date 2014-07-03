package no.raiom.tls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TempLogProfile {
    public final byte[] random;
    private final int sample_interval;
    private final int base_sample_num;
    private final long base_sample_ts;

    TempLogProfile(byte[] random, int sample_interval, int base_sample_num, long base_sample_ts) {
        this.random          = random;
        this.sample_interval = sample_interval;
        this.base_sample_num = base_sample_num;
        this.base_sample_ts  = base_sample_ts;
    }

    static TempLogProfile from_byte_data(long now, byte[] desc1, byte[] desc2) {
        byte random[] = Arrays.copyOfRange(desc1, 0, 16);
        int  sample_interval =       (desc1[16] & 0xff) + ((desc1[17] & 0xff) << 8);
        int  base_sample_num =       (desc2[ 0] & 0xff) + ((desc2[ 1] & 0xff) << 8);
        long base_sample_ts  = now - (desc2[ 2] & 0xff) + ((desc2[ 3] & 0xff) << 8);

        return new TempLogProfile(random, sample_interval, base_sample_num, base_sample_ts);
    }

    List<TempLogSample> decode_samples(byte[] data) {
        List<TempLogSample> samples = new ArrayList<TempLogSample>();
        byte flags = data[0];
        int num_logs = (int) data[1] & 0xff;
        int first_sample_num = (int) ((data[2] & 0xff) + ((data[3] & 0xff) << 8));
        for (int i = 0; i < num_logs; i++) {
            int sample_num = first_sample_num + i;
            double sample = 0.0625 * ((data[4 + (2 * i)] & 0xff) + ((data[5 + (2 * i)] & 0xff) << 8));
            long ts = base_sample_ts + ((sample_num - base_sample_num) * sample_interval);
            samples.add(new TempLogSample(sample_num, sample, sample_num * sample_interval, ts));
        }

        return samples;
    }

    public class TempLogSample {
        private final int sample_number;
        private final double sample;
        private final long seconds_since_start;
        private final long ts;

        TempLogSample(int sample_number, double sample, long seconds_since_start, long ts) {
            this.sample_number = sample_number;
            this.sample = sample;
            this.seconds_since_start = seconds_since_start;
            this.ts = ts;
        }

        @Override
        public String toString() {
            return String.format("(% 4d, % 3.2f, %s, %s)",
                    sample_number, sample, seconds_since_start, ts);
        }
    }
}
