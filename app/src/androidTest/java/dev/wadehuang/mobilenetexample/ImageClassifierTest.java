package dev.wadehuang.mobilenetexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.List;

import dev.wadehuang.mobilenetexample.images.ImageClassifier;
import dev.wadehuang.mobilenetexample.images.Recognition;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ImageClassifierTest {
    @Test
    public void recognizeImage() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        ImageClassifier imageClassifier = new ImageClassifier(appContext);

        InputStream stream = appContext.getAssets().open("example_image.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        List<Recognition> results = imageClassifier.recognizeImage(bitmap);

        assertEquals(results.get(0).getId(), "818");
    }

}