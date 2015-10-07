/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 

/**
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * updateLayout(layout): update the layout of the active tab
 * 
 * updateActiveSheet(change) : update the sheet after tools value changed 
 * 
 * validate(): return null if the panel is valid, else return a validationError for each sheet
 * setSheetsState(state): set the state of the panels
 * getSheetsState(): get the state of the panel
 * 
 * Public Events
 * 
 * tabChange(activePanel): the tab is changed
 * 
 * Authors - Antonella Giachino
 */

Ext.define('Sbi.cockpit.core.SheetsContainerPanel', {
	extend: 'Ext.TabPanel'
	, layout:'fit'
	, plugins:	Ext.create('Sbi.cockpit.core.SheetTabMenu',{showCloseAll:false,
															showCloseOthers: false,
															listeners: {

		sheetremove: function(tab){		
			//removes the selected widget container from the internal list
			this.tabPanel.widgetContainerList.remove(tab);
		},
		sheetremoveothers: function(tab){		
			//removes all widget container except the selected one from the internal list
			for (var i=0; i < this.tabPanel.widgetContainerList.length; i++){
				var tmpWc = this.tabPanel.widgetContainerList[i];
				if (tmpWc.id !== tab.id){
					this.tabPanel.widgetContainerList.remove(tmpWc);
				}
			}
		},
		sheetremoveall: function(tab){		
			//remove all widget containers from the internal list
			for (var i=0; i < this.tabPanel.widgetContainerList.length; i++){
				var tmpWc = this.tabPanel.widgetContainerList[i];				
					this.tabPanel.widgetContainerList.remove(tmpWc);		
			}
		}
		}})

          
	, config:{
		cls: 'sheetsContainer',
		border: false,
		tabPosition: 'bottom',        
        enableTabScroll:true,
        defaults: {autoScroll:true},
        frame: true,
    	index: 0,
    	sheetId: 0
	}


	/**
	 * @property {Sbi.data.AssociationEditorWizardPanel} editorMainPanel
	 *  Container of the wizard panel
	 */
	, containerPanelList: null

	, constructor : function(config) {
		Sbi.trace("[SheetsContainerPanel.constructor]: IN");
		
		this.initConfig(config);
		
		this.callParent(arguments);
		
		this.initPanel();
		this.initEvents();
		
		this.addEvents("sheetremove", "sheetremoveothers", "sheetremoveall");

		Sbi.trace("[SheetsContainerPanel.constructor]: OUT");
	}	

	
	// -----------------------------------------------------------------------------------------------------------------
    //  methods
	// -----------------------------------------------------------------------------------------------------------------

	, addTab: function(sheetConf){
		//add a new tab		
		this.suspendEvents();
		if (this.widgetContainerList == null) this.widgetContainerList = new Array(); 
			
		this.remove('addTab');

		var addPanel = {
			id: 'addTab',
	        title: '<br>',
	        iconCls: 'newTabIcon'
		};
		

		var conf = {};
		if(Sbi.isValorized(this.lastSavedAnalysisState) && !Sbi.isValorized(sheetConf) ) {
			conf = this.lastSavedAnalysisState.widgetsConf;
		}		
		 
		if (Sbi.isValorized(conf) && !Sbi.isValorized(sheetConf)){
			//execution: cycle for view all tabs
			for (var j=0; j<conf.length; j++){
				var sheetConf = conf[j].sheetConf;
				var widgetContainer = new Sbi.cockpit.core.WidgetContainer(sheetConf);
				
				var sheet =  widgetContainer; 
				sheet.id = this.sheetId; //conf[j].sheetId;
				sheet.title= conf[j].sheetTitle;
				sheet.index = this.index;
		        sheet.closable=  (Sbi.config.documentMode === 'EDIT');
		        sheet.bodyCls= this.bodyCls;   
		        // adds the newest widgetContainer into the global list
		        this.widgetContainerList.push(sheet);
		        var tab = this.add(sheet);
		        if (j<conf.length-1) {
		        	//update index for the default name
		        	this.index = this.index + 1;		        	
		        }
		        //update the id of the sheet (unique for all sheets)
		        this.sheetId = this.sheetId +1;
			}
		}else {
			//creation: add one tab anytime
			var widgetContainer = new Sbi.cockpit.core.WidgetContainer(conf);
			
			var sheet =  widgetContainer; 
			sheet.id = this.sheetId;
			sheet.title= 'Sheet ' + this.sheetId;
			sheet.index = this.index;
	        sheet.closable= (Sbi.config.documentMode === 'EDIT');
	        sheet.bodyCls= this.bodyCls; 
	        // adds the newest widgetContainer into the global list
	        this.widgetContainerList.push(sheet);
	        var tab = this.add(sheet);
	        this.index = this.index + 1;
	        this.sheetId = this.sheetId +1;
		}
        
	    if (Sbi.config.documentMode === 'EDIT'){
	    	this.add(addPanel);   
	    }

	    if(this.getActiveTab()==null){
	    	this.setActiveTab(0);
	    	this.widgetContainer = this.activeTab; //update of main widget manager with the newest one
	    }
	    
	    this.resumeEvents();
	    
	    tab.on('beforeClose',function(panel){
			Ext.MessageBox.confirm(
					LN('sbi.cockpit.msg.deletetab.title'),
					LN('sbi.cockpit.msg.deletetab.msg'),            
		            function(btn, text) {
		                if (btn=='yes') {		                
		                	this.hideElementsOfTab(panel);
		                	this.widgetContainerList.remove(panel);
		                	this.remove(panel);		                	
		                }
		            },
		            this
				);
			return false;
	    }, this);
	    
	}
	 
	, manageVisibilityTabsWindows: function(tabPanel, tab){		
		
		Sbi.trace("[SheetsContainerPanel.manageVisibilityTabsWindows]: IN");
		this.suspendEvents();
		
		
		for (var i=0; i<this.widgetContainerList.length; i++){
			var currentTab = this.widgetContainerList[i];
			
			//hides all windows of other sheets
			if (currentTab.id !== tab.id){
				if (Sbi.isValorized(currentTab.getComponents())){
					var tabWindows =  currentTab.getComponents();
					if (Sbi.isValorized(tabWindows) && tabWindows.length>0 ){						
						for (var j=0; j<tabWindows.length; j++){
							var w = tabWindows[j];
							w.hide();
						}
					}else{
						Sbi.trace("[SheetsContainerPanel.manageVisibilityTabsWindows]: widgetContainer " + tab.id + 
										" hasn't got any windows to hide! ");
					}
				
				}
			}
			
			//shows all windows of selected sheets
			if (currentTab.id === tab.id){
				if (Sbi.isValorized(currentTab.getComponents())){
					var tabWindows = currentTab.getComponents();
					if (Sbi.isValorized(tabWindows) && tabWindows.length>0 ){
						for (var j=0; j<tabWindows.length; j++){
							var w = tabWindows[j];
							w.show();
						}
					}else{
						Sbi.trace("[SheetsContainerPanel.manageVisibilityTabsWindows]: widgetContainer " + tab.id + 
										" hasn't got any windows to show! ");
					}
				}
			}
		}
		
		this.resumeEvents();
		Sbi.trace("[SheetsContainerPanel.manageVisibilityTabsWindows]: OUT");
	}
	
	// Hides  all elements (windows) of input tab
	, hideElementsOfTab: function(tab){
		var tabWindows =  tab.getComponents();
		if (Sbi.isValorized(tabWindows) && tabWindows.length>0 ){						
			for (var j=0; j<tabWindows.length; j++){
				var w = tabWindows[j];
				w.hide();
			}
		}
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[SheetsContainerPanel.init]: IN");
		//add init methods here
		Sbi.trace("[SheetsContainerPanel.init]: OUT");
	}

	, initEvents: function() {
		Sbi.trace("[SheetsContainerPanel.initEvents]: IN");
		
		
		this.on('render',function(){ 			
							this.index = 1;
							this.addTab(); 
						},
						this);

		Sbi.trace("[SheetsContainerPanel.initEvents]: OUT");
	}
	
	, initPanel: function(){
		Sbi.trace("[SheetsContainerPanel.initPanel]: IN");
		
		this.sheetId = 1;
		
		this.on('tabchange',function(tabPanel, tab){
	    	if(tab==null || tab.id=='addTab'){
	    		this.addTab('addTab');
//	    	    tabPanel.setActiveTab(tabPanel.items.length-2);
	    	    tabPanel.setActiveTab(tabPanel.items.length-1);
	    	}else{
	    		this.manageVisibilityTabsWindows(tabPanel, tab);
	    		//update contents of new tab
	    		var associationGroups = Sbi.storeManager.getAssociationGroups();
				var selections = this.widgetContainer.getWidgetManager().getSelectionsByAssociations();
				
		    	if(Sbi.isValorized(associationGroups) && Sbi.isValorized(selections)) {
		    		for (s in selections){
		    			var actualSelection = selections[s];
		    			if (Sbi.isValorized(actualSelection) && actualSelection.length>0){
							for (var i=0; i < associationGroups.length; i++ ){
								var associationGroup = associationGroups[i];
								this.widgetContainer.widgetManager.applySelectionsOnAssociationGroup(associationGroup);	
							}
		    			}
		    		}
		    	}
	    	}

	    	this.widgetContainer = this.activeTab; //update of main widget manager with the active one
	    },this);
				
		
		Sbi.trace("[SheetsContainerPanel.initPanel]: OUT");
	}
	
});