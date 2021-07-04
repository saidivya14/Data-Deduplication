package utils;

import static data.deduplication.AdminHome.UploadFileStatus;
import java.util.ArrayList;
import java.util.List;

public class FileOperations {
    private List<FileChunkInfoAdapter> chunkInfo;
    private final FileHandler fileHandler;
    private final DbHelper dbHelper;
    
    public FileOperations(){
        fileHandler = new FileHandler();
        dbHelper = new DbHelper();
        chunkInfo = new ArrayList<>();
    }
    
    public int uploadFile(String filePath){   
        UploadFileStatus.append("Splitting file into chnuks and calculating checksum...\n");
        UploadFileStatus.append("Default chunk size: " + CommonProperties.chunkSize/1024 + "Kb\n\n");
        UploadFileStatus.update(UploadFileStatus.getGraphics());  
        chunkInfo = fileHandler.split(filePath, CommonProperties.chunkSize);
        long sizeOccupiedOnStorage = 0;
                    
        UploadFileStatus.append("Checking for duplicate chunks...\n\n");
        UploadFileStatus.update(UploadFileStatus.getGraphics());
        sizeOccupiedOnStorage = chunkInfo.stream().map(item -> dbHelper.insertChunksRecordInDB(item.checkSum, item.filePath, String.valueOf(item.fileName), item.fileOriginalName, item.fileExtension, item.chunkSize)).reduce(sizeOccupiedOnStorage, (accumulator, _item) -> accumulator + _item);
        UploadFileStatus.append("\nTotal size occupied on storage after upload: " + sizeOccupiedOnStorage/1024 + "Kb\n\n");
        UploadFileStatus.append("Updating hash indexes of chunks...\n\n");
        UploadFileStatus.update(UploadFileStatus.getGraphics());
        
        return (int) sizeOccupiedOnStorage;
    }
    
    public void downloadFile(String fileDirectory){      
        fileHandler.join(CommonProperties.outputFilePath, 
                fileDirectory, 
                dbHelper.getFileExtension(fileDirectory), 
                dbHelper.selectChunksPath(fileDirectory));
    }
}
