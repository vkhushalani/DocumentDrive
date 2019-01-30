package com.ngahr.documentDrive.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

public class CustomDocument {
	private String uniqueName ;
	private String uniqueKey;

	Logger logger = LoggerFactory.getLogger(CustomDocument.class);
	public EcmService getECMService() throws NamingException{
        InitialContext ctx = new InitialContext();
        String lookupName = "java:comp/env/" + "EcmService";
        EcmService ecmSvc = (EcmService) ctx.lookup(lookupName);
        return ecmSvc;
	}
	public RepositoryOptions getRepository(){
		
		RepositoryOptions options = new RepositoryOptions();
        options.setUniqueName(uniqueName);
        options.setRepositoryKey(uniqueKey);
        options.setVisibility(Visibility.PROTECTED);
        return options;
	}
	public EcmService CreateRepository(EcmService ecmSvc,RepositoryOptions rep){
		ecmSvc.createRepository(rep);
		return ecmSvc;
		}
	public Folder createNewFolder(Folder folder,String fname,String createdBy){
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, fname);
		 properties.put(PropertyIds.CREATED_BY, createdBy);
		 properties.put(PropertyIds.CREATION_DATE, new Date());
	      return folder.createFolder(properties);
	}
	public Document createNewDocument (Session openCmisSession,Folder folder,String docName,byte[] Content, String mimeType,String createdBy, long l) throws UnsupportedEncodingException{
		 Map<String, Object> properties = new HashMap<String, Object>();
	      properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
	      properties.put(PropertyIds.NAME, docName);
	      properties.put(PropertyIds.CREATED_BY, createdBy);
	      properties.put(PropertyIds.CREATION_DATE, new Date());
	      properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType);
	      InputStream stream = new ByteArrayInputStream(Content);
	      ContentStream contentStream = openCmisSession.getObjectFactory()
	                                    .createContentStream(docName,
	                                    Content.length,mimeType,stream);
	     
	       return folder.createDocument(properties,contentStream, VersioningState.NONE);
		
	}
	public Session createSession(EcmService ecmSvc){
		 try
		 {
			 return ecmSvc.connect(uniqueName,uniqueKey); 
		 }
		 catch (CmisObjectNotFoundException e) {
			 
			 RepositoryOptions rep = getRepository();
			 ecmSvc = CreateRepository(ecmSvc, rep);
			 return ecmSvc.connect(uniqueName,uniqueKey); 
			 
		 }
	}
	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {	
		this.uniqueName = uniqueName;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	public Folder getFolderByName(Folder parentFolder,String folderName){
		for (CmisObject child :parentFolder.getChildren())
			{
				if(child instanceof Folder)
				{
					if(child.getName().equalsIgnoreCase(SharedConstants.INI_FOLDER))
					{
						return (Folder)child;
					}
				}
			}
		return null;
		
	}
	public Document getDocumentByObjectId(Session openCmisSession,String docId){
		Document doc = (Document) openCmisSession.getObject(docId);
        return doc;
	}
	public Folder getFolderByObjectId(Session openCmisSession,String folderId){
		Folder folder = (Folder) openCmisSession.getObject(folderId);
        return folder;	
	}
	
	public List<CmisObject> getFolderItemsByObjectId(Session openCmisSession,String folderId){
		Folder folder = (Folder) openCmisSession.getObject(folderId);
        List<CmisObject> items = new ArrayList<CmisObject>();
        
        for (CmisObject child: folder.getChildren()) {
            items.add(child);
        }
		return items;	
	}
}
