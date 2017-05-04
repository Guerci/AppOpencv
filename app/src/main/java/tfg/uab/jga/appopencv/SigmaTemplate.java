package tfg.uab.jga.appopencv;

import org.opencv.core.Mat;

/**
 * Created by jordi on 04/05/2017.
 */

public class SigmaTemplate {
    Mat SigmaCentre;
    Mat SigmaSurround;

    public SigmaTemplate(Mat sigmaCentre, Mat sigmaSurround) {
        SigmaCentre = sigmaCentre;
        SigmaSurround = sigmaSurround;
    }



    public Mat getSigmaCentre(){
        return SigmaCentre;
    }
    public Mat getSigmaSurround(){
        return SigmaSurround;
    }

}
