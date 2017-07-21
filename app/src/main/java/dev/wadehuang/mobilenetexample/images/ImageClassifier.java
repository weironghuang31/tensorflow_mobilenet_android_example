package dev.wadehuang.mobilenetexample.images;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

public class ImageClassifier {
    public static final int INPUT_SIZE = 224;

    private static final float THRESHOLD = 0.1f;
    private static final int MAX_RESULTS = 3;
    private static final String MODEL_FILE = "mobilenet_v1.pb";
    private static final String LABEL_FILE = "labels.txt";
    private static final int CLASS_SIZE = 1001;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "MobilenetV1/Predictions/Reshape_1";
    private static final String[] OUTPUT_NAMES = {OUTPUT_NAME};

    private Context context;
    private Operation output_op;
    private TensorFlowInferenceInterface tfInterface;
    private Vector<String> labels;

    public ImageClassifier(Context context) {
        this.context = context;

        this.tfInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);

        InitLabels();
    }

    private void InitLabels(){
        labels = new Vector<>(CLASS_SIZE);
        try {
            BufferedReader br = null;
            InputStream stream  = context.getAssets().open(LABEL_FILE);
            br = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        }
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        return  recognizeImage(ImageHelper.bitmapToFloat(bitmap));
    }

    public List<Recognition> recognizeImage(final float[] imageFloats) {

        this.tfInterface.feed(INPUT_NAME, imageFloats, 1, INPUT_SIZE, INPUT_SIZE, 3);

        this.tfInterface.run(OUTPUT_NAMES, false);

        float[] outputs = new float[CLASS_SIZE];
        this.tfInterface.fetch(OUTPUT_NAME, outputs);

        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] > THRESHOLD) {
                pq.add(new Recognition("" + i, labels.get(i), outputs[i], null));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();

        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }

}
