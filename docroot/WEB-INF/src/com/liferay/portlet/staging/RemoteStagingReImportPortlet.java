package com.liferay.portlet.staging;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.StagingServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/**
 * Portlet implementation class RemoteStagingReImportPortlet
 */
public class RemoteStagingReImportPortlet extends MVCPortlet {

	public void reImport(ActionRequest request, ActionResponse actionResponse) {
		String filenameAndPath = request.getParameter("filenameAndPath");
		String propertiesFileNameAndPath = request
				.getParameter("propertiesFileNameAndPath");
		boolean privateLayout = ParamUtil.getBoolean(request, "privateLayout",
				false);
		long remoteGroupId = ParamUtil.getLong(request, "remoteGroupId", -1);

		if (filenameAndPath == null) {
			SessionErrors
					.add(request, "LAR File name with path is not defined");
			
			_log.error("no lar file defined");
			return;
		}

		if (propertiesFileNameAndPath == null) {
			SessionErrors.add(request,
					"Properties file name with path is not defined");
			_log.error("no Properties file defined");
			return;
		}

		File larFile = new File(filenameAndPath);

		if (!larFile.exists()) {
			SessionErrors.add(request, filenameAndPath + " does not exist");
			_log.error("no lar file");
			return;
		}

		File propertiesFile = new File(propertiesFileNameAndPath);

		if (!propertiesFile.exists()) {
			SessionErrors.add(request, propertiesFileNameAndPath
					+ " does not exist");
			_log.error("no properties file");
			return;
		}
		
		try {
			GroupLocalServiceUtil.getGroup(remoteGroupId);
		}
		catch (Exception pe) {
			SessionErrors.add(request, "Group " + remoteGroupId + " does not exist");
			_log.error("Group " + remoteGroupId + " does not exist");
			return;
		}
		

		FileInputStream fileInputStream = null;
		
		try {


			Map<String, String[]> parameterMap = readProperties(propertiesFileNameAndPath);
			
			String checksum = FileUtil.getMD5Checksum(larFile);

			long stagingRequestId = StagingServiceUtil.createStagingRequest(
					remoteGroupId, checksum);
			
			_log.error("larFile " + larFile.getName() + " size " + larFile.length() + " ");
			
			fileInputStream = new FileInputStream(larFile);

			
			
			long timer = 0L;
			long start = System.currentTimeMillis();
			
			int STAGING_REMOTE_TRANSFER_BUFFER_SIZE = 650*1024*1024;
			
			byte[] bytes =
					new byte[STAGING_REMOTE_TRANSFER_BUFFER_SIZE];
				
				
			int i = 0;
			int counter = 1;
			while ((i = fileInputStream.read(bytes)) > 0) {
				if (i < STAGING_REMOTE_TRANSFER_BUFFER_SIZE) {
					byte[] tempBytes = new byte[i];

					System.arraycopy(bytes, 0, tempBytes, 0, i);

					StagingServiceUtil.updateStagingRequest(stagingRequestId, larFile.getName(),
						tempBytes);
				}	
				else {
					StagingServiceUtil.updateStagingRequest(stagingRequestId, larFile.getName(), bytes);
				}

				bytes =	new byte[STAGING_REMOTE_TRANSFER_BUFFER_SIZE];
				
				timer = System.currentTimeMillis() - start;
				_log.error(counter + " StagingServiceHttp.updateStagingRequest() timespent " + timer + " Size: " + i);
				counter++;
			}			
			
			
			
			StagingServiceUtil.validateStagingRequest(stagingRequestId,
					privateLayout, parameterMap);
			
			
			timer = System.currentTimeMillis() - start;
			
			_log.error("validateStagingRequest " + timer);
			
			StagingServiceUtil.publishStagingRequest(stagingRequestId,
					privateLayout, parameterMap);
			timer = System.currentTimeMillis() - start;
			
			_log.error("publishStagingRequest " + timer);

			
			
			StagingServiceUtil.cleanUpStagingRequest(stagingRequestId);
			timer = System.currentTimeMillis() - start;
			
			_log.error("cleanUpStagingRequest " + timer);			

			SessionMessages.add(request, "success");
		} catch (IOException e) {
			SessionErrors.add(request, e.getMessage());
			_log.error(e);
		} catch (PortalException e) {
			SessionErrors.add(request, e.getMessage());
			_log.error(e);
		} catch (SystemException e) {
			SessionErrors.add(request, e.getMessage());
			_log.error(e);
		}
		finally {
			StreamUtil.cleanUp(fileInputStream);
		}
	}

	public Map<String, String[]> readProperties(String propertiesFileNameAndPath)
			throws IOException {
		UnicodeProperties unicodeProperties = new UnicodeProperties();
		String props;
		props = FileUtil.read(propertiesFileNameAndPath);

		unicodeProperties.load(props);

		Map<String, String[]> map = new HashMap<String, String[]>();

		for (Entry<String, String> entry : unicodeProperties.entrySet()) {
			String key = entry.getKey();

			if (key.startsWith("$@$")) {
				int startPos = key.lastIndexOf("$@$") + "$@$".length();

				if (startPos == -1) {
					continue;
				}

				key = key.substring(startPos);

				String[] oldStrings = map.get(key);

				if (oldStrings == null) {
					String[] strings = { entry.getValue() };
					map.put(key, strings);
				} else {
					String[] strings = new String[oldStrings.length + 1];
					strings[oldStrings.length] = entry.getValue();
					map.put(key, strings);
				}
			} else {
				String[] strings = { entry.getValue() };
				map.put(key, strings);
			}
		}

		return map;
	}

	private Log _log = LogFactoryUtil
			.getLog(RemoteStagingReImportPortlet.class);
}
