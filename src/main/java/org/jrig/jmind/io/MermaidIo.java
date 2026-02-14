/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.io;

// import java.io.File;
// import java.io.IOException;
// import java.nio.file.Files;
// // import java.nio.file.Paths;
// import java.util.ArrayList;
// import java.util.List;

// import com.jrig.speccommander.model.Diagram;
// import com.jrig.speccommander.model.DiagramBlock;
// import com.jrig.speccommander.model.DiagramRelation;
// import com.jrig.speccommander.model.NoteBlock;
// import com.jrig.speccommander.model.ParameterBlock;
// import com.jrig.speccommander.model.Spec;
// import com.jrig.speccommander.model.SpecBlock;

public class MermaidIo {

    // public static List<String> saveLines(Spec spec, Diagram diagram) throws
    // IOException {
    // List<String> lines = new ArrayList<>();

    // lines.add("requirementDiagram\n\n");

    // for (DiagramBlock block : diagram.blocks) {
    // if (block instanceof SpecBlock) {

    // lines.add("requirement " + ((SpecBlock) block).getUUID() + " {\n" +
    // "\tid: " + ((SpecBlock) block).getUUID() + " \n" +
    // "\ttext: " + ((SpecBlock) block).specObject.name + " \n" +
    // "\trisk: " + ((SpecBlock) block).specObject.risk + " \n" +
    // "\tverifymethod: " + ((SpecBlock) block).specObject.verification + " \n" +
    // "}\n\n");

    // } else if (block instanceof ParameterBlock) {
    // lines.add("element " + ((ParameterBlock) block).getUUID() + " {\n" +
    // "\ttype: parameter\n" +
    // "}\n\n");

    // } else { // NoteBlock
    // lines.add("element " + ((NoteBlock) block).getUUID() + " {\n" +
    // "\ttype: note\n" +
    // "}\n\n");
    // }
    // }

    // for (DiagramRelation relation : spec.relations) {
    // if (diagram.uuidRelations.contains(relation.uuid)) {
    // lines.add("{ " + relation.uuidFrom + " } - " + relation.relationType + " -> {
    // " + relation.uuidTo
    // + " }\n\n");
    // }
    // }

    // return lines;
    // }

    // public static void save(File file, Spec spec, Diagram diagram) throws
    // IOException {
    // Files.write(file.toPath(), saveLines(spec, diagram));
    // }

}
