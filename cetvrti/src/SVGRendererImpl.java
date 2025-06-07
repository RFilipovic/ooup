
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SVGRendererImpl implements Renderer {

    private List<String> lines = new ArrayList<>();
    private String fileName;

    public SVGRendererImpl(String fileName) {
        this.fileName = fileName;
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        lines.add("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"600\">");
    }

    public void close() throws IOException {
        lines.add("</svg>");

        try (FileWriter writer = new FileWriter(fileName)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    @Override
    public void drawLine(Point s, Point e) {
        String line = String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"black\" stroke-width=\"1\" />",
                s.getX(), s.getY(), e.getX(), e.getY());
        lines.add(line);
    }

    @Override
    public void fillPolygon(Point[] points) {
        StringBuilder pointsStr = new StringBuilder();
        for (int i = 0; i < points.length; i++) {
            if (i > 0) pointsStr.append(" ");
            pointsStr.append(points[i].getX()).append(",").append(points[i].getY());
        }

        String polygon = String.format("<polygon points=\"%s\" style=\"stroke: black; fill: gray; stroke-width: 1;\" />",
                pointsStr.toString());
        lines.add(polygon);
    }
}