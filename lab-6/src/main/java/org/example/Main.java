import javafx.application.Application;
import org.example.Data;
import org.example.DendrogramGUI;
import org.example.HierarchicalClustering;

void main() {
//    double[][] objects = new double[4][4];
//    objects[0][1] = objects[1][0] = 5;
//    objects[0][2] = objects[2][0] = 0.5;
//    objects[0][3] = objects[3][0] = 2;
//    objects[1][2] = objects[2][1] = 1;
//    objects[1][3] = objects[3][1] = 0.6;
//    objects[2][3] = objects[3][2] = 2.5;
//    Data data = new Data(objects);

//     HierarchicalClustering.hierarchyByMin(data);
//     HierarchicalClustering.hierarchyByMax(data);

    Application.launch(DendrogramGUI.class);
}

