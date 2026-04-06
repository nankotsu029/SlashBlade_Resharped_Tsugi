package mods.flammpfeil.slashblade.client.renderer.model.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Wavefront Object importer Based heavily off of the specifications found at
 * <a href="http://en.wikipedia.org/wiki/Wavefront_.obj_file">...</a>
 */
public class WavefrontObject {
    public ArrayList<Vertex> vertices = new ArrayList<>();
    public ArrayList<Vertex> vertexNormals = new ArrayList<>();
    public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<>();
    public ArrayList<GroupObject> groupObjects = new ArrayList<>();
    private GroupObject currentGroupObject;
    private final String fileName;

    public WavefrontObject(ResourceLocation resource) throws ModelFormatException {
        this.fileName = resource.toString();

        try {
            loadObjModel(Minecraft.getInstance().getResourceManager().open(resource));
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        }
    }

    public WavefrontObject(String filename, InputStream inputStream) throws ModelFormatException {
        this.fileName = filename;
        loadObjModel(inputStream);
    }

    private void loadObjModel(InputStream inputStream) throws ModelFormatException {
        BufferedReader reader = null;

        String currentLine;
        int lineCount = 0;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((currentLine = reader.readLine()) != null) {
                lineCount++;
                currentLine = currentLine.replaceAll("\\s+", " ").trim();

                if (!currentLine.startsWith("#") && !currentLine.isEmpty()) {
                    if (currentLine.startsWith("v ")) {
                        Vertex vertex = parseVertex(currentLine, lineCount);
                        if (vertex != null) {
                            vertices.add(vertex);
                        }
                    } else if (currentLine.startsWith("vn ")) {
                        Vertex vertex = parseVertexNormal(currentLine, lineCount);
                        if (vertex != null) {
                            vertexNormals.add(vertex);
                        }
                    } else if (currentLine.startsWith("vt ")) {
                        TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                        if (textureCoordinate != null) {
                            textureCoordinates.add(textureCoordinate);
                        }
                    } else if (currentLine.startsWith("f ")) {

                        if (currentGroupObject == null) {
                            currentGroupObject = new GroupObject("Default");
                        }

                        Face face = parseFace(currentLine, lineCount);

                        currentGroupObject.faces.add(face);
                    } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                        GroupObject group = parseGroupObject(currentLine, lineCount);

                        if (group != null) {
                            if (currentGroupObject != null) {
                                groupObjects.add(currentGroupObject);
                            }
                        }

                        currentGroupObject = group;
                    }
                }
            }
            if (currentGroupObject != null) {
                groupObjects.add(currentGroupObject);
            }
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // hush
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                // hush
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tessellateAll(VertexConsumer tessellator, PoseStack matrixStack, int light, int color) {
        for (GroupObject groupObject : groupObjects) {
            groupObject.render(tessellator, matrixStack, light, color);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tessellateOnly(VertexConsumer tessellator, PoseStack matrixStack, int light, int color, String... groupNames) {
        for (GroupObject groupObject : groupObjects) {
            for (String groupName : groupNames) {
                if (groupName.equalsIgnoreCase(groupObject.name)) {
                    groupObject.render(tessellator, matrixStack, light, color);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tessellatePart(VertexConsumer tessellator, PoseStack matrixStack, int light, int color, String partName) {
        for (GroupObject groupObject : groupObjects) {
            if (partName.equalsIgnoreCase(groupObject.name)) {
                groupObject.render(tessellator, matrixStack, light, color);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tessellateAllExcept(VertexConsumer tessellator, PoseStack matrixStack, int light, int color, String... excludedGroupNames) {
        boolean exclude;
        for (GroupObject groupObject : groupObjects) {
            exclude = false;
            for (String excludedGroupName : excludedGroupNames) {
                if (excludedGroupName.equalsIgnoreCase(groupObject.name)) {
                    exclude = true;
                    break;
                }
            }
            if (!exclude) {
                groupObject.render(tessellator, matrixStack, light, color);
            }
        }
    }

    private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
        String[] tokens = tokenize(line);

        if (!isValidNumericLine(tokens, "v", 3, 4)) {
            throw invalidFormat(line, lineCount);
        }

        try {
            if (tokens.length == 3) {
                return new Vertex(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            }
            return new Vertex(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
        } catch (NumberFormatException e) {
            throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
        }
    }

    private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {
        String[] tokens = tokenize(line);

        if (!isValidNumericLine(tokens, "vn", 3, 3)) {
            throw invalidFormat(line, lineCount);
        }

        try {
            return new Vertex(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
        } catch (NumberFormatException e) {
            throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
        }
    }

    private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {
        String[] tokens = tokenize(line);

        if (!isValidNumericLine(tokens, "vt", 2, 3)) {
            throw invalidFormat(line, lineCount);
        }

        try {
            if (tokens.length == 3) {
                return new TextureCoordinate(Float.parseFloat(tokens[1]), 1 - Float.parseFloat(tokens[2]));
            }
            return new TextureCoordinate(Float.parseFloat(tokens[1]), 1 - Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3]));
        } catch (NumberFormatException e) {
            throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
        }
    }

    private Face parseFace(String line, int lineCount) throws ModelFormatException {
        Face face;

        if (isValidFaceLine(line)) {
            face = new Face();

            String trimmedLine = line.substring(line.indexOf(" ") + 1);
            String[] tokens = trimmedLine.split(" ");
            String[] subTokens;

            if (tokens.length == 3) {
                if (currentGroupObject.glDrawingMode == -1) {
                    currentGroupObject.glDrawingMode = GL11.GL_TRIANGLES;
                } else if (currentGroupObject.glDrawingMode != GL11.GL_TRIANGLES) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount
                            + ") in file '" + fileName + "' - Invalid number of points for face (expected 4, found "
                            + tokens.length + ")");
                }
            } else if (tokens.length == 4) {
                if (currentGroupObject.glDrawingMode == -1) {
                    currentGroupObject.glDrawingMode = GL11.GL_QUADS;
                } else if (currentGroupObject.glDrawingMode != GL11.GL_QUADS) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount
                            + ") in file '" + fileName + "' - Invalid number of points for face (expected 3, found "
                            + tokens.length + ")");
                }
            }

            // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
            if (isValidFace_V_VT_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                    face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1/vt1 v2/vt2 v3/vt3 ...
            else if (isValidFace_V_VT_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1//vn1 v2//vn2 v3//vn3 ...
            else if (isValidFace_V_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("//");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1 v2 v3 ...
            else if (isValidFace_V_Line(line)) {
                face.vertices = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    face.vertices[i] = vertices.get(Integer.parseInt(tokens[i]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else {
                throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount
                        + ") in file '" + fileName + "' - Incorrect format");
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '"
                    + fileName + "' - Incorrect format");
        }

        return face;
    }

    private GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
        String[] tokens = tokenize(line);
        if (tokens.length < 2 || (!"g".equals(tokens[0]) && !"o".equals(tokens[0]))) {
            throw invalidFormat(line, lineCount);
        }

        String trimmedLine = line.substring(line.indexOf(" ") + 1).trim();
        return trimmedLine.isEmpty() ? null : new GroupObject(trimmedLine);
    }

    /***
     * Verifies that the given line from the model file is a valid vertex
     *
     * @param line the line being validated
     * @return true if the line is a valid vertex, false otherwise
     */
    private static boolean isValidVertexLine(String line) {
        return isValidNumericLine(tokenize(line), "v", 3, 4);
    }

    /***
     * Verifies that the given line from the model file is a valid vertex normal
     *
     * @param line the line being validated
     * @return true if the line is a valid vertex normal, false otherwise
     */
    private static boolean isValidVertexNormalLine(String line) {
        return isValidNumericLine(tokenize(line), "vn", 3, 3);
    }

    /***
     * Verifies that the given line from the model file is a valid texture
     * coordinate
     *
     * @param line the line being validated
     * @return true if the line is a valid texture coordinate, false otherwise
     */
    private static boolean isValidTextureCoordinateLine(String line) {
        return isValidNumericLine(tokenize(line), "vt", 2, 3);
    }

    /***
     * Verifies that the given line from the model file is a valid face that is
     * described by vertices, texture coordinates, and vertex normals
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f
     *         v1/vt1/vn1 ..." (with a minimum of 3 points in the face, and a
     *         maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_VN_Line(String line) {
        String[] tokens = tokenize(line);
        return hasValidFaceArity(tokens) && areValidFaceTokens(tokens, token -> {
            String[] subTokens = token.split("/");
            return subTokens.length == 3
                    && !subTokens[0].isEmpty()
                    && !subTokens[1].isEmpty()
                    && !subTokens[2].isEmpty()
                    && areIntegers(subTokens);
        });
    }

    /***
     * Verifies that the given line from the model file is a valid face that is
     * described by vertices and texture coordinates
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1
     *         ..." (with a minimum of 3 points in the face, and a maximum of 4),
     *         false otherwise
     */
    private static boolean isValidFace_V_VT_Line(String line) {
        String[] tokens = tokenize(line);
        return hasValidFaceArity(tokens) && areValidFaceTokens(tokens, token -> {
            String[] subTokens = token.split("/");
            return subTokens.length == 2
                    && !subTokens[0].isEmpty()
                    && !subTokens[1].isEmpty()
                    && areIntegers(subTokens);
        });
    }

    /***
     * Verifies that the given line from the model file is a valid face that is
     * described by vertices and vertex normals
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1//vn1
     *         ..." (with a minimum of 3 points in the face, and a maximum of 4),
     *         false otherwise
     */
    private static boolean isValidFace_V_VN_Line(String line) {
        String[] tokens = tokenize(line);
        return hasValidFaceArity(tokens) && areValidFaceTokens(tokens, token -> {
            String[] subTokens = token.split("//");
            return subTokens.length == 2
                    && !subTokens[0].isEmpty()
                    && !subTokens[1].isEmpty()
                    && areIntegers(subTokens);
        });
    }

    /***
     * Verifies that the given line from the model file is a valid face that is
     * described by only vertices
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1 ..."
     *         (with a minimum of 3 points in the face, and a maximum of 4), false
     *         otherwise
     */
    private static boolean isValidFace_V_Line(String line) {
        String[] tokens = tokenize(line);
        return hasValidFaceArity(tokens) && areValidFaceTokens(tokens, WavefrontObject::isInteger);
    }

    /***
     * Verifies that the given line from the model file is a valid face of any of
     * the possible face formats
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches any of the valid face
     *         formats, false otherwise
     */
    private static boolean isValidFaceLine(String line) {
        return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line)
                || isValidFace_V_Line(line);
    }

    /***
     * Verifies that the given line from the model file is a valid group (or object)
     *
     * @param line the line being validated
     * @return true if the line is a valid group (or object), false otherwise
     */
    private static boolean isValidGroupObjectLine(String line) {
        String[] tokens = tokenize(line);
        return tokens.length >= 2 && ("g".equals(tokens[0]) || "o".equals(tokens[0]));
    }

    public String getType() {
        return "obj";
    }

    private static String[] tokenize(String line) {
        return line.trim().split("\\s+");
    }

    private static boolean isValidNumericLine(String[] tokens, String prefix, int minValues, int maxValues) {
        if (tokens.length < minValues + 1 || tokens.length > maxValues + 1 || !prefix.equals(tokens[0])) {
            return false;
        }

        for (int i = 1; i < tokens.length; i++) {
            if (!isFloat(tokens[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasValidFaceArity(String[] tokens) {
        return tokens.length >= 4 && tokens.length <= 5 && "f".equals(tokens[0]);
    }

    private static boolean areValidFaceTokens(String[] tokens, java.util.function.Predicate<String> validator) {
        for (int i = 1; i < tokens.length; i++) {
            if (!validator.test(tokens[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean areIntegers(String[] tokens) {
        for (String token : tokens) {
            if (!isInteger(token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFloat(String token) {
        try {
            Float.parseFloat(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private ModelFormatException invalidFormat(String line, int lineCount) {
        return new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '"
                + fileName + "' - Incorrect format");
    }
}
