package gsg;

import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Wow, legacy stuff!
 */
public class WaveFrontUtils {

    public static class WaveFrontObj {
        List<VectorUtils.Vector3D> vertices;
        List<int[]> lines;

        public WaveFrontObj(List<VectorUtils.Vector3D> vertices,
                List<int[]> lines) {
            this.vertices = vertices;
            this.lines = lines;
        }

        public List<VectorUtils.Vector3D> getVertices() {
            return vertices;
        }

        public List<int[]> getLines() {
            return lines;
        }
    }

    public static WaveFrontObj parseWaveFrontObj(BufferedReader objStream) {
        String line = "";
        List<VectorUtils.Vector3D> vertices = new ArrayList<>();
        List<int[]> lines = new ArrayList<>();

        try {
            while ((line = objStream.readLine()) != null) {
                if (line.startsWith("#")) {
                    // Skip, it's a comment
                } else {
                    String[] lineData = line.split(" ");
                    if (lineData[0].equals("v")) { // A vertex
                        vertices.add(new VectorUtils.Vector3D(Double.parseDouble(lineData[1]),
                                Double.parseDouble(lineData[2]),
                                Double.parseDouble(lineData[3])));
                    } else if (lineData[0].equals("f")) { // A line
                        lines.add(new int[] {Integer.parseInt(lineData[1]),
                                Integer.parseInt(lineData[2])});
                    }
                }
            }
            objStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new WaveFrontObj(vertices, lines);
    }
}
