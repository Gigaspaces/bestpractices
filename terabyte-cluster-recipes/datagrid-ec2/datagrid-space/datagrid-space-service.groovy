service {
	
	icon "icon.png"
	name "datagrid-space"
	statefulProcessingUnit {
		binaries "datagrid-space" //can be a folder, jar or a war file   		
		sla {
			memoryCapacity 2048000
			maxMemoryCapacity 2048000
			highlyAvailable true
			memoryCapacityPerContainer 64000
		}
	}
}