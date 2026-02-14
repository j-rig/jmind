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

public class DotIo {
    // public static void save(File file, Spec spec, Diagram diagram) throws
    // IOException {

    // List<String> lines = new ArrayList<>();

    // for (DiagramBlock block : diagram.blocks) {
    // if (block instanceof SpecBlock) {

    // lines.add(((SpecBlock) block).getUUID() + " [label=\"requirement: "
    // + ((SpecBlock) block).specObject.name + "\"]\n\n");

    // } else if (block instanceof ParameterBlock) {
    // lines.add(((ParameterBlock) block).getUUID()
    // + " [label=\"" + ((ParameterBlock) block).parameter.name
    // + "=" + ((ParameterBlock) block).parameter.value + " "
    // + ((ParameterBlock) block).parameter.units + "\"]\n\n");

    // } else { // NoteBlock
    // lines.add(((NoteBlock) block).getUUID() + " [label=\"note: "
    // + ((NoteBlock) block).note + "\"]\n\n");
    // }
    // }

    // for (DiagramRelation relation : spec.relations) {
    // if (diagram.uuidRelations.contains(relation.uuid)) {
    // lines.add(
    // relation.uuidFrom + "->" + relation.uuidTo + " [label=\"" +
    // relation.relationType + "\"]\n\n");
    // }
    // }

    // Files.write(file.toPath(), lines);

    // }

}
