service {
	
	icon "icon.png"
	name "datagrid-space"
	statefulProcessingUnit {
		binaries "datagrid-space" //can be a folder, jar or a war file   		
		sla {
			memoryCapacity 1000000
			maxMemoryCapacity 1000000
			highlyAvailable true
			memoryCapacityPerContainer 64000
		}
	}
}