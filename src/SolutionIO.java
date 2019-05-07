import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.*;
import java.util.HashMap;
import java.io.FileWriter;

public class SolutionIO {

public Solution read(String File_Name) {
	File file_read = new File(File_Name);
	BufferedReader Buff_Reader = null;
	Solution Sol_Created = new Solution();
	Sol_Created.evacNodesList = new HashMap<Integer,EvacNodeData>();
	Sol_Created.other ="";
	String line;
	int instance = 0;
	try{
		Buff_Reader = new BufferedReader(new FileReader(file_read));
		while ((line = Buff_Reader.readLine()) != null){
			String[] parts = line.split("\\s+");
			if(instance==0){
				Sol_Created.instanceName = line;
				instance ++;
			}
			else if(instance==1){
				Sol_Created.evacNodesNB = Integer.valueOf(parts[0]);
				instance ++;
			}
			else if (instance==2){
					if (parts[0].equals("valid")){
						Sol_Created.nature = true;
						instance++;
					}
					else if (parts[0].equals("valid"))
					{
						Sol_Created.nature = false;
						instance++;
					}
					else
					{
						//try{
						Sol_Created.evacNodesList.put(Integer.valueOf(parts[0]),new EvacNodeData(Integer.valueOf(parts[1]),Integer.valueOf(parts[2])));
						/*}
						catch(NullPointerException e)
						{
							System.out.println(parts[0]+" "+parts[1]+" "+parts[2]);
						}*/
					}
				}
			else if (instance==3)
			{
				Sol_Created.objectiveValue = Integer.valueOf(parts[0]);
				instance++;
			}
			else if (instance==4)
			{
				Sol_Created.computeTime = Integer.valueOf(parts[0]);
				instance++;
			}
			else if (instance==5)
			{
				Sol_Created.method = line;
				instance++;
			}
			else 
			{
				Sol_Created.other = Sol_Created.other+ line +"\n";
			}
		}
	} catch (IOException e)
	{
		System.out.println("Error read solution");
	}
		return Sol_Created;
}
	


public int write(Solution sol, String File_Name) {
	FileWriter fWrit;
	File filew = new File(File_Name);
	try{
	filew.delete();
	fWrit = new FileWriter(filew, true);
	fWrit.write(sol.instanceName+"\n");
	fWrit.write(sol.evacNodesNB+"\n");
	for(int origin : sol.evacNodesList.keySet())
	{
		fWrit.write(origin+" "+sol.evacNodesList.get(origin).beginDate+" "+sol.evacNodesList.get(origin).evacRate+"\n");
	}
	if (sol.nature)
	{
		fWrit.write("valid \n");
	}
	else
	{
		fWrit.write("invalid \n");
	}
	fWrit.write(sol.objectiveValue+"\n");
	fWrit.write(sol.computeTime+"\n");
	fWrit.write(sol.method+"\n");
	fWrit.write(sol.other);
	fWrit.flush();
	fWrit.close();

	}
	catch (IOException e){
		System.err.println("Create FileWRITER FAIL");
		e.printStackTrace();
	}

	return 0;


}
}