import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.*;



// corriger le type float
public class dataReader {

    public data Convert_File_In_Data(String File_Name){

        File file_read = new File(File_Name);
        BufferedReader Buff_Reader = null;
		data Data_Created = new data();
    	String line;
        boolean definition = true;
        int evacinfo = 0;
        //int numb =0;
		try{
		Buff_Reader = new BufferedReader(new FileReader(file_read));
		while ((line = Buff_Reader.readLine()) != null){
           
              String[] parts = line.split("\\s+");
    
           if(parts[0].equals("c"))
            {
                definition = true;
                evacinfo++;
            }
            else{
                if(definition){
                    if(evacinfo==1){             	    
                        Data_Created.set_num_evac(Integer.valueOf(parts[0]));
                        Data_Created.set_safe_node(Integer.valueOf(parts[1]));
                        definition = false;
                    }
                    else{
                        Data_Created.set_nb_node(Integer.valueOf(parts[0]));
                        Data_Created.set_nb_edge(Integer.valueOf(parts[1]));    
                        definition = false;    
                    }
                }

                else
                {
                    if(evacinfo==1){
                        ArrayList<Integer> listPath= new ArrayList<Integer>();
                        for (int i = 4; i< parts.length; i++)
                        {
                            listPath.add(Integer.valueOf(parts[i]));
                        }
                        Data_Created.add_evac_path(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3]), listPath );
                    }
                    else{
                        try{
                        Data_Created.add_edge(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3]), Integer.valueOf(parts[4]));
                        }catch(NumberFormatException e)
                        {
                            Data_Created.add_edge(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Integer.MAX_VALUE, Integer.valueOf(parts[3]), Integer.valueOf(parts[4]));
                        }
                        
                    }
                }
                
            }
			}

      	} catch(IOException e)
      	{
		System.out.println("Error dataReader");
        }
          
    return Data_Created;
    }
    
}
