package com.ngahr.documentDrive.controller;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.ArrayList;
//import java.sql.Timestamp;
//import java.util.Calendar;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sap.ecm.api.EcmService;
import com.ngahr.documentDrive.model.CustomDocument;
import com.ngahr.documentDrive.model.DocObject;
import com.ngahr.documentDrive.model.MyFolder;
import com.ngahr.documentDrive.model.SharedConstants;



@RestController
public class DocumentObjectController {
	Logger logger = LoggerFactory.getLogger(DocumentObjectController.class);
	
	 @GetMapping("/pDrive/initialFolder")
	 public ResponseEntity<?> intialFolderCreation() throws NamingException{
		 
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession =cDoc.createSession(ecmSvc);
		 Folder ifolder;
		 Folder rootFolder = openCmisSession.getRootFolder();
			
		 ifolder = cDoc.getFolderByName(rootFolder, SharedConstants.INI_FOLDER);
		 if(ifolder == null)
		 {  ifolder = cDoc.createNewFolder(rootFolder,SharedConstants.INI_FOLDER,"");}
	     
		 return ResponseEntity.ok().body(ifolder.getId());
	 }
	 @DeleteMapping("/pDrive/document/{docId}")
	 public ResponseEntity<?> deleteFile(@PathVariable("docId") String docId) throws NamingException {
		 
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession = ecmSvc.connect(cDoc.getUniqueName(),cDoc.getUniqueKey());
		 
		 Document doc = cDoc.getDocumentByObjectId(openCmisSession, docId);
		 doc.delete(true);
		 return ResponseEntity.ok().body("Successfully Deleted DocId:" + docId);
		 
	 }
	 @DeleteMapping("/pDrive/folder/{folderId}")
	 public ResponseEntity<?> deleteFolder(@PathVariable("folderId") String folderId) throws NamingException {
		 
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession = ecmSvc.connect(cDoc.getUniqueName(),cDoc.getUniqueKey());
		 
		 Folder folder = cDoc.getFolderByObjectId(openCmisSession, folderId);
		 folder.deleteTree(true,UnfileObject.DELETE,true);
		 return ResponseEntity.ok().body("Successfully Deleted DocId:" + folderId);
		 
	 }
	 @PostMapping("/pDrive/folder/{folderId}/document")
	 public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile,HttpServletRequest request,@PathVariable(value = "folderId") String folderId) throws NamingException, UnsupportedEncodingException, IOException{
		 String loggedUserId =  request.getUserPrincipal().getName().toUpperCase();
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession =cDoc.createSession(ecmSvc);
		 Folder folder;
		 folder = cDoc.getFolderByObjectId(openCmisSession,folderId);
		 InputStream stream = new ByteArrayInputStream(uploadfile.getBytes());	 	 
		 String mimeType = URLConnection.guessContentTypeFromStream(stream);;
	        if (mimeType == null || mimeType.length() == 0) {
	            mimeType = "application/octet-stream";
	        }
//	        uploadfile.getS
//	     Timestamp timestamp = new Timestamp(System.currentTimeMillis()); 
		 Document doc = cDoc.createNewDocument(openCmisSession, folder, uploadfile.getOriginalFilename(),uploadfile.getBytes(),mimeType,loggedUserId,uploadfile.getSize()); 

		 return ResponseEntity.ok().body("Successfully Uploaded DocId:" + doc.getId());

	 }
	 @PostMapping("/pDrive/folder")
	 public ResponseEntity<?> newFolder(HttpServletRequest request,@RequestBody MyFolder mFolder) throws NamingException, UnsupportedEncodingException, IOException{
		 String loggedUserId =  request.getUserPrincipal().getName().toUpperCase();
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession =cDoc.createSession(ecmSvc);
		 Folder folder;
		 folder = cDoc.getFolderByObjectId(openCmisSession, mFolder.getParentFolderId());
			 	 
		 Folder nfolder = cDoc.createNewFolder(folder, mFolder.getFolderName(), loggedUserId);
	     
		 return ResponseEntity.ok().body("Successfully Created Folder:" + nfolder.getId());

	 }
	 
	 @GetMapping("/pDrive/folder/{folderId}/items")
	 public ResponseEntity<List<DocObject>> getChildItems(@PathVariable("folderId") String folderId) throws NamingException {
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession =cDoc.createSession(ecmSvc);
		 Folder folder;
		 folder = cDoc.getFolderByObjectId(openCmisSession, folderId);
		 List<CmisObject> items = cDoc.getFolderItemsByObjectId(openCmisSession, folder.getId());
		 List<DocObject> docObjects = new ArrayList<DocObject>();
		 for(CmisObject item : items)
		 {	 
			 DocObject docObject = new DocObject();
			 if(item instanceof Document)
			 {
				 Document aDoc = (Document) item;
				 docObject.setId(aDoc.getId());
				 docObject.setName(aDoc.getName());
				
				 docObject.setType("FILE");
				 docObject.setCreateBy(aDoc.getCreatedBy());
				 docObject.setCreatedOn(aDoc.getCreationDate().getTime());
				 docObject.setSize(String.valueOf(aDoc.getContentStreamLength()));
				 
			 }
			 else if (item instanceof Folder)
			 {
				 Folder aFolder = (Folder) item;
				 docObject.setId(aFolder.getId());
				 docObject.setName(aFolder.getName());
				 docObject.setType("FOLDER");
				 docObject.setCreateBy(aFolder.getCreatedBy());
				 docObject.setCreatedOn(aFolder.getCreationDate().getTime());
//				 ;
			 } 
			 docObjects.add(docObject);
		 }
		return ResponseEntity.ok().body(docObjects);
	 }
	 @GetMapping("/pDrive/document/{docId}/view")
	 public ResponseEntity<?> viewDocument(@PathVariable("docId") String docId,HttpServletResponse response) throws NamingException, IOException{
		 
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession = ecmSvc.connect(cDoc.getUniqueName(),cDoc.getUniqueKey());
		 Document Doc = cDoc.getDocumentByObjectId(openCmisSession, docId);
		 ContentStream docStream = Doc.getContentStream();
		
		 if (docStream != null) {
             response.setContentType(Doc.getContentStreamMimeType());
             response.setHeader("Content-disposition", "inline; filename=\""+Doc.getName()+ "\"");
             IOUtils.copy(docStream.getStream(), response.getOutputStream());
             IOUtils.closeQuietly(docStream.getStream());
             response.flushBuffer();
         }
		 return null; 
	 }
	 @GetMapping("/pDrive/document/{docId}/download")
	 public ResponseEntity<?> downloadDocument(@PathVariable("docId") String docId,HttpServletResponse response) throws NamingException, IOException{
		 
		 CustomDocument cDoc = new CustomDocument();
		 cDoc.setUniqueKey(SharedConstants.REP_KEY);
		 cDoc.setUniqueName(SharedConstants.REP_NAME);
		 
		 Session openCmisSession = null;
		 
		 EcmService ecmSvc = cDoc.getECMService();
		 openCmisSession = ecmSvc.connect(cDoc.getUniqueName(),cDoc.getUniqueKey());
		 Document Doc = cDoc.getDocumentByObjectId(openCmisSession, docId);
		 ContentStream docStream = Doc.getContentStream();
		
		 if (docStream != null) {
             response.setContentType(Doc.getContentStreamMimeType());
             response.setHeader("Content-disposition", "attachment; filename=\""+Doc.getName()+ "\"");
             IOUtils.copy(docStream.getStream(), response.getOutputStream());
             IOUtils.closeQuietly(docStream.getStream());
             response.flushBuffer();
         }
		 return null; 
	 }
}
