package org.openspaces.example.helloworld.common;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.polling.trigger.TriggerOperationHandler;
import org.springframework.dao.DataAccessException;

import com.j_spaces.core.client.SQLQuery;

public class MyTrigger implements TriggerOperationHandler {

	private GigaSpace clusteredGigaSpace;

	@Override
	public Object triggerReceive(Object t, GigaSpace gigaSpace,
			long receiveTimeout) throws DataAccessException {

		// Make the thread wait for new data with a blocking read with timeout.
		// Otherwise trigger operation handler will keep getting invoked in a
		// tight loop
		Message template = new Message();
		template.setInfo("Hello ");
		Message newMsg = gigaSpace.read((Message) template, 60000);

		if (newMsg != null) {
			SQLQuery<Message> query = new SQLQuery<Message>(Message.class,
					"processed = false ORDER BY id DESC");

			Message localObject = (Message) gigaSpace.read(query);

			// If there is an object matching the template, validate if this is
			// right priority
			if (localObject != null) {

				Message clusteredObject = clusteredGigaSpace.read(query);

				if (clusteredObject != null
						&& localObject.getId().equals(clusteredObject.getId())) {
					return localObject;
				}
			}
		}
		return null;
	}

	@Override
	public boolean isUseTriggerAsTemplate() {
		return true;
	}

	public GigaSpace getClusteredGigaSpace() {
		return clusteredGigaSpace;
	}

	public void setClusteredGigaSpace(GigaSpace clusteredGigaSpace) {
		this.clusteredGigaSpace = clusteredGigaSpace;
	}

}
