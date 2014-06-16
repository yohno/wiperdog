/**
 * Get data to create tree menu
 */

class GenerateTreeMenu {
	/**
	 * getData2CreateMenu: get data to create tree menu
	 * @Param JOB_DIR: job directory
	 * @Return data2CreateMenu
	 */
	public static getData2CreateMenu(JOB_DIR) {
		def data2CreateMenu = [:]
		def shell = new GroovyShell()

		// Get list job
		def job_dir = new File(JOB_DIR)
		def list_job = []
		if(job_dir.isDirectory()){
			job_dir.listFiles().each {file ->
				def fileName = file.getName()
				if(fileName.endsWith('.job')){
					list_job.add(fileName)
				}
			}
		}

		def dbmsInfoFile = new File(MonitorJobConfigLoader.getProperties().get(ResourceConstants.DBMS_INFO))
		def dbmsInfo = shell.evaluate(dbmsInfoFile.getText())
		def root = dbmsInfo['TreeMenuInfo']

		//Initial
		def output = [:]
		def tmpKey = ""
		root.each{k,v->
			if (!v.isEmpty()) {
				output[k]=[:]
				v.each{c, valuec->
					tmpKey = k + "." + c
					output[tmpKey] = []
				}
			} else {
				output[k] = []
			}
		}

		//Bind job name to create tree menu
		def isOthersJob
		def isOthersJobInGroup
		list_job.each {
			isOthersJob = true
			def tmpArray = it.split("\\.")
			if (tmpArray.size() >= 2) {
				root.each{k,v->
					if (tmpArray[0] == k) {
						if (!v.isEmpty()) {
							if (tmpArray.size() >= 3) {
								isOthersJobInGroup = true
								v.each{c, valuec->
									if (tmpArray[1] == c) {
										tmpKey = k + "." + c
										output[tmpKey].add(it)
										isOthersJobInGroup = false
										//Set to not add in others group
										isOthersJob = false
									}
								}
								if (isOthersJobInGroup) {
									tmpKey = k + ".Others"
									output[tmpKey].add(it)
									//Set to not add in others group
									isOthersJob = false
								}
							}
						} else {
							if ((tmpArray.size() == 3) && (output[k] instanceof List)) {
								output[k].add(it)						
								//Set to not add in others group
								isOthersJob = false
							}
						}	
					}
				}
			}
			if (isOthersJob) {
				output["Others"].add(it)
			}
		}
		data2CreateMenu['root'] = root
		data2CreateMenu['output'] = output
		return data2CreateMenu
	}

	/**
	 * Recursively function, used for gen tree menu data 
	 * Input treeItem: root tree map of menu (not leaf)
	 *       mapCollection: Map of collections, Item of map has key is a job group and value is list job which is applied for that group
	 *       parentList: used for recursively to canculate key if data if leaf
	 * Output: If data is leaf, create key of group data, get list of job which is applied for group from mapCollection and create menu Item
	 *         If data isn't leaf, create node and call recursively function with sub data
	 **/
	public static getMenuItemsStr(treeItem, mapCollection, parentList = []) {     
	     def ul_open = false
	     def result = ""
	     def parentStr = ""
	     def parentLstforChild = []

	     //If data isn't leaf, create node and call recursively function with sub data
	     if (treeItem instanceof Map) {
	         result += "<ul id='treemenu2' class='treeview'>"
	         treeItem.each{itemKey, itemVal -> 
	             parentList.each{parentListItem->
	                 parentLstforChild.add(parentListItem)
	             }
	             parentLstforChild.add(itemKey)
		     result += "<li>"+ itemKey
	             result += getMenuItemsStr(itemVal, mapCollection, parentLstforChild)
	             result +="</li>"
	             parentLstforChild = []
	         }
	         result += "</ul>"
	     }
	     
	     //If data is leaf, create key of group data, get list of job which is applied for group from mapCollection and create menu Item
	     if (treeItem instanceof List) {
	         result += "<ul>"
	         parentList.each{parentItem -> 
	              if (parentStr != ""){
	                  parentStr += "."
	              }
	              parentStr += parentItem
	         }
	         if (mapCollection[parentStr] != null) {
	             mapCollection[parentStr].each {item->
	                 result += "<li><a>" + item +"</a></li>"
	             }
	         }
	         result += "</ul>"
	     }
	     return result
	}
}