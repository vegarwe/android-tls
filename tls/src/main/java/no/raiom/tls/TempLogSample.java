package no.raiom.tls;

import java.util.ArrayList;
import java.util.List;

public class TempLogSample {
    private final int    sample_number;
    private final double sample;

    TempLogSample (int sample_number, double sample) {
        this.sample_number = sample_number;
        this.sample        = sample;
    }

    @Override
    public String toString() {
        return String.format("(% 4d, % 3.2f)", this.sample_number, this.sample);
    }

    static List<TempLogSample> decode_samples(byte[] data) {
        byte flags      =         data[0];
        int  num_logs   = (int)   data[1] & 0xff;
        int  sample_num = (int) ((data[2] & 0xff) + ((data[3] & 0xff) << 8));
        List<TempLogSample> samples = new ArrayList<TempLogSample>();
        for (int i = 0; i < num_logs; i++) {
            int sample  = (int) ((data[4+(2*i)] & 0xff) + ((data[5+(2*i)] & 0xff) << 8));
            samples.add(new TempLogSample(sample_num + i, sample * 0.0625));
        }

        return samples;
    }
}
