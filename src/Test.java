import java.util.ArrayList;
import java.util.HashMap;

public class Test {

public static void main(String[] args) {

    /* On crée l'exemple du TD */
    dataReader reader = new dataReader();
    data TD_data = new data();
    TD_data = reader.Convert_File_In_Data("donneetest.full");
    TD_data.read_data();
    }

}
