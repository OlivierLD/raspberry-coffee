package gsg;

import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Very basic for now.
 */
public class WaveFrontUtils {

    public static class WaveFrontObj {
        List<VectorUtils.Vector3D> vertices;
        List<int[]> edges;

        public WaveFrontObj(List<VectorUtils.Vector3D> vertices,
                List<int[]> edges) {
            this.vertices = vertices;
            this.edges = edges;
        }
        public List<VectorUtils.Vector3D> getVertices() {
            return vertices;
        }
        public List<int[]> getEdges() {
            return edges;
        }
    }

    public static WaveFrontObj parseWaveFrontObj(BufferedReader objStream) {
        String line = "";
        List<VectorUtils.Vector3D> vertices = new ArrayList<>();
        List<int[]> edges = new ArrayList<>();

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
                        edges.add(new int[] {Integer.parseInt(lineData[1]),
                                Integer.parseInt(lineData[2])});
                    }
                }
            }
            objStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new WaveFrontObj(vertices, edges);
    }
}
