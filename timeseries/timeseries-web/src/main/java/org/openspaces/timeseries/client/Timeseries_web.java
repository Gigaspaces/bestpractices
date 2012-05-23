package org.openspaces.timeseries.client;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.timeseries.shared.Tick;

import com.extjs.gxt.charts.client.Chart;
import com.extjs.gxt.charts.client.model.ChartModel;
import com.extjs.gxt.charts.client.model.ToolTip;
import com.extjs.gxt.charts.client.model.ToolTip.MouseStyle;
import com.extjs.gxt.charts.client.model.axis.Label;
import com.extjs.gxt.charts.client.model.axis.XAxis;
import com.extjs.gxt.charts.client.model.axis.YAxis;
import com.extjs.gxt.charts.client.model.charts.BarChart;
import com.extjs.gxt.charts.client.model.charts.BarChart.Bar;
import com.extjs.gxt.charts.client.model.charts.HorizontalBarChart;
import com.extjs.gxt.charts.client.model.charts.LineChart;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.FastSet;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Timeseries_web implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	int pollms=2000;
	private final Boolean[] running={false};

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */

	private final TicksServiceAsync ticksService= GWT.create(TicksService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		String url = "chart/open-flash-chart.swf";    

		RootPanel rootPanel = RootPanel.get("base");
		rootPanel.setHeight("");

		LayoutContainer baseContainer = new LayoutContainer();
		baseContainer.setLayout(new RowLayout(Orientation.VERTICAL));
		
		LayoutContainer title=new LayoutContainer();
		title.setLayout(new FitLayout());
		
		com.google.gwt.user.client.ui.Label lblNewLabel = new com.google.gwt.user.client.ui.Label("TimeSeries Demo");
		lblNewLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		title.add(lblNewLabel);
		lblNewLabel.setHeight("25px");
		baseContainer.add(title);

		final Chart lineChart = new Chart(url);
		lineChart.setChartModel(getLineChartModel());

		final Chart barChart = new Chart(url);
		barChart.setChartModel(getBarChartModel());
		final BarChart bc=(BarChart)barChart.getChartModel().getChartConfigs().get(0);
		
		final com.extjs.gxt.ui.client.widget.form.FormPanel frmpnlTickgenerator = new com.extjs.gxt.ui.client.widget.form.FormPanel();
		frmpnlTickgenerator.setHeading("TickGenerator");
		frmpnlTickgenerator.setCollapsible(true);

		FieldSet fldstSymbolInfo = new FieldSet();
		fldstSymbolInfo.setVisible(true);
		FormLayout fl_fldstSymbolInfo = new FormLayout();
		fl_fldstSymbolInfo.setDefaultWidth(100);
		fldstSymbolInfo.setLayout(fl_fldstSymbolInfo);

		final TextField<String> txtfldSymbol = new TextField<String>();
		txtfldSymbol.setMaxLength(4);
		//frmpnlTickgenerator.add(txtfldSymbol, new FormData("100%"));
		fldstSymbolInfo.add(txtfldSymbol);
		txtfldSymbol.setFieldLabel("Symbol");

		final NumberField nmbrfldBasis = new NumberField();
		nmbrfldBasis.setAllowBlank(false);
		nmbrfldBasis.setAllowNegative(false);
		//frmpnlTickgenerator.add(nmbrfldBasis, new FormData("100%"));
		nmbrfldBasis.setFieldLabel("Price Basis");
		fldstSymbolInfo.add(nmbrfldBasis);

		final NumberField nmbrfldMinvol = new NumberField();
		nmbrfldMinvol.setAllowBlank(false);
		nmbrfldMinvol.setAllowDecimals(false);
		nmbrfldMinvol.setAllowNegative(false);
		fldstSymbolInfo.add(nmbrfldMinvol);
		nmbrfldMinvol.setFieldLabel("Min Vol");

		final NumberField nmbrfldMaxVol = new NumberField();
		nmbrfldMaxVol.setFieldLabel("Max Vol");
		nmbrfldMaxVol.setAllowNegative(false);
		nmbrfldMaxVol.setAllowDecimals(false);
		nmbrfldMaxVol.setAllowBlank(false);
		fldstSymbolInfo.add(nmbrfldMaxVol);
		frmpnlTickgenerator.add(fldstSymbolInfo, new RowData(265.0, 140.0, new Margins()));
		fldstSymbolInfo.setHeight("145px");
		fldstSymbolInfo.setHeading("Symbol Info");

		FieldSet fldstControl = new FieldSet();
		FormLayout fl_fldstControl = new FormLayout();
		fl_fldstControl.setLabelWidth(50);
		fl_fldstControl.setLabelPad(4);
		fldstControl.setLayout(fl_fldstControl);

		Button btnStart = new Button("Start",new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(!frmpnlTickgenerator.isValid()){
					MessageBox.alert("Error","Invalid entries",new Listener<MessageBoxEvent>(){
						@Override
						public void handleEvent(MessageBoxEvent be) {
						}});
				}
				List<List<String>> syms=new ArrayList<List<String>>();
				List<String> sym=new ArrayList<String>();
				sym.add(txtfldSymbol.getValue());
				sym.add(nmbrfldMinvol.getValue().toString());
				sym.add(nmbrfldMaxVol.getValue().toString());
				sym.add(nmbrfldBasis.getValue().toString());
				syms.add(sym);
				ticksService.setConfig(syms,new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) {
						MessageBox.alert("Error",caught.getMessage(),new Listener<MessageBoxEvent>(){
							@Override
							public void handleEvent(MessageBoxEvent be) {
							}});
					}
					@Override
					public void onSuccess(Void result) {
						ticksService.start(new AsyncCallback<Void>(){
							
						
							@Override
							public void onFailure(Throwable caught) {
								MessageBox.alert("Error",caught.getMessage(),new Listener<MessageBoxEvent>(){
									@Override
									public void handleEvent(MessageBoxEvent be) {
									}});
							}
							@Override
							public void onSuccess(Void result) {
								running[0]=true;
							}

						});
					}
				});
			}

		});
		fldstControl.add(btnStart, new FormData("100%"));

		Button btnStop = new Button("Stop",new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				ticksService.stop(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) {
						MessageBox.alert("Error",caught.getMessage(),new Listener<MessageBoxEvent>(){
							@Override
							public void handleEvent(MessageBoxEvent be) {
							}});
					}
					@Override
					public void onSuccess(Void result) {
						running[0]=false;
					}

				});
			}

		});

		fldstControl.add(btnStop, new FormData("100%"));

		Button btnClear = new Button("Clear Space",new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				ticksService.clearSpace(new AsyncCallback<Void>(){
					@Override
					public void onFailure(Throwable caught) {
						MessageBox.alert("Error",caught.getMessage(),new Listener<MessageBoxEvent>(){
							@Override
							public void handleEvent(MessageBoxEvent be) {
							}});
					}

					@Override
					public void onSuccess(Void result) {
					}
					
				});
			}
			
		});
		fldstControl.add(btnClear, new FormData("100%"));

		Text txtSpacer = new Text("");
		txtSpacer.setEnabled(false);
		fldstControl.add(txtSpacer, new FormData("100%"));

		final TextField<String> txtfldTickCnt = new TextField<String>();
		txtfldTickCnt.setValue("0");
		txtfldTickCnt.setVisible(true);
		txtfldTickCnt.setReadOnly(true);
		txtfldTickCnt.setAllowBlank(false);
		FormData fd_txtfldTickCnt = new FormData("100%");
		fd_txtfldTickCnt.setMargins(new Margins(0, 0, 0, 0));
		fldstControl.add(txtfldTickCnt, fd_txtfldTickCnt);
		txtfldTickCnt.setFieldLabel("Tick Cnt");
		frmpnlTickgenerator.add(fldstControl, new RowData(140.0, 140.0, new Margins(0, 0, 0, 0)));
		fldstControl.setHeight("145px");
		fldstControl.setHeading("Control");
		baseContainer.add(frmpnlTickgenerator, new RowData(635.0, 185.0, new Margins()));
		frmpnlTickgenerator.setLayout(new RowLayout(Orientation.HORIZONTAL));

		ContentPanel cpAnalytics=new ContentPanel();
		baseContainer.add(cpAnalytics);
		cpAnalytics.setHeading("Analytics");
		cpAnalytics.setFrame(true);
		cpAnalytics.setSize(640,200);
		cpAnalytics.setLayout(new FitLayout());
		cpAnalytics.setHeaderVisible(true);
		cpAnalytics.add(lineChart);
		
		ContentPanel cpVolume = new ContentPanel();
		cpVolume.setSize(640,200);
		cpVolume.setHeaderVisible(false);
		cpVolume.setBorders(false);
		cpVolume.setLayout(new FitLayout());
		cpVolume.add(barChart);
		cpVolume.setSize(640, 80);
		baseContainer.add(cpVolume);
		baseContainer.setSize("640px", "474px");
		rootPanel.add(baseContainer);
		rootPanel.setWidgetPosition(baseContainer, 0, 0);

		new Timer(){
			@Override
			public void run() {
				long now=System.currentTimeMillis();
				final long endperiod=now-(now%pollms)-1;

				if(txtfldSymbol.getValue().length()>0){
					ticksService.getTicks(txtfldSymbol.getValue(), new String[]{"vwap","average","volatility","volume"}, endperiod-pollms, endperiod, new AsyncCallback<Tick[]>(){
						@Override
						public void onFailure(Throwable caught) {
							MessageBox.alert("Error",caught.getMessage(),new Listener<MessageBoxEvent>(){
								@Override
								public void handleEvent(MessageBoxEvent be) {
								}});
						}
						@Override
						public void onSuccess(Tick[] result) {
							if(result!=null){
								FastSet missing=new FastSet();
								missing.add("vwap");
								missing.add("vol");
								missing.add("ave");
								missing.add("volume");
								final LineChart lc=(LineChart)lineChart.getChartModel().getChartConfigs().get(0);
								for(Tick tick:result){
									if(tick.getType().equals("vwap")){
										((LineChart)lineChart.getChartModel().getChartConfigs().get(0)).addValues(Double.valueOf(tick.getValue().get(0)));
										missing.remove("vwap");
									}
									else if(tick.getType().equals("average")){
										((LineChart)lineChart.getChartModel().getChartConfigs().get(1)).addValues(Double.valueOf(tick.getValue().get(0)));
										missing.remove("ave");
									}
									else if(tick.getType().equals("volatility")){
										((LineChart)lineChart.getChartModel().getChartConfigs().get(2)).addValues(Double.valueOf(tick.getValue().get(0)));
										missing.remove("vol");
									}
									else if(tick.getType().equals("volume")){
										bc.addBars(new Bar(Integer.valueOf(tick.getValue().get(0))/1000));
										missing.remove("volume");
									}
									else{
										MessageBox.alert("Error","invalid tick type:"+tick.getType(),new Listener<MessageBoxEvent>(){
											@Override
											public void handleEvent(MessageBoxEvent be) {
											}});
										
									}
								}
								for(String m:missing){
									if(m.equals("vwap")){
										((LineChart)lineChart.getChartModel().getChartConfigs().get(0)).addNullValue();
									}
									else if(m.equals("ave")){
										((LineChart)lineChart.getChartModel().getChartConfigs().get(1)).addNullValue();
									}
									else if(m.equals("vol")){
										((LineChart)lineChart.getChartModel().getChartConfigs().get(2)).addNullValue();
									}
									else if(m.equals("volume")){
										bc.addNullValue();
									}
								}
								lineChart.refresh();
								barChart.refresh();
							}
						}

					});
					
				}
			}
		}.scheduleRepeating(pollms);

		new Timer(){
			@Override
			public void run(){
				ticksService.getTotalTicks(new AsyncCallback<Integer>(){
					@Override
					public void onFailure(Throwable caught) {
						txtfldTickCnt.setValue("err");
					}

					@Override
					public void onSuccess(Integer result) {
						txtfldTickCnt.setValue(String.valueOf(result));

					}

				});
			}
		}.scheduleRepeating(5000);

	}
	
	public ChartModel getLineChartModel()   
	{   
		//Create a ChartModel with the Chart Title and some style attributes  
		ChartModel cm = new ChartModel("Analytics", "font-size: 14px; font-family: Verdana; text-align: center;");  

		XAxis xa = new XAxis();  
		//set the maximum, minimum and the step value for the X axis  
		xa.setRange(0, 300, 20);    
		xa.setGridColour("#777777");
		xa.setColour("#777777");
		cm.setXAxis(xa);

		YAxis ya = new YAxis();  
		ya.setOffset(true);    
		ya.setRange(0,20,5);
		ya.setGridColour("#777777");
		ya.setColour("#777777");
		cm.setYAxis(ya);


		LineChart lchart_vwap=new LineChart();
		lchart_vwap.setModel(cm);
		lchart_vwap.setColour("#aa0000");
		lchart_vwap.setRightAxis(true);

		LineChart lchart_ave=new LineChart();
		lchart_ave.setModel(cm);
		lchart_ave.setColour("#00aa00");
		
		LineChart lchart_vol=new LineChart();
		lchart_vol.setModel(cm);
		lchart_vol.setColour("#0000aa");
		
		cm.addChartConfig(lchart_vwap);
		cm.addChartConfig(lchart_ave);
		cm.addChartConfig(lchart_vol);
		cm.setBackgroundColour("#f7f0e9");

		cm.setTooltipStyle(new ToolTip(MouseStyle.FOLLOW));    
		return cm;    
	}
	
	public ChartModel getBarChartModel(){
		//Create a ChartModel with the Chart Title and some style attributes  
		ChartModel cm = new ChartModel("", "font-size: 14px; font-family: Verdana; text-align: center;");  

		XAxis xa = new XAxis();  
		//set the maximum, minimum and the step value for the X axis  
		xa.setRange(0, 300, 20);    
		xa.setGridColour("#777777");
		xa.setColour("#777777");
		cm.setXAxis(xa);

		YAxis ya = new YAxis();  
		ya.setRange(0,10,5);
		ya.setOffset(true);    
		ya.setGridColour("#777777");
		ya.setColour("#777777");
		cm.setYAxis(ya);

		BarChart bchart=new BarChart();
		bchart.setModel(cm);
		cm.addChartConfig(bchart);
		
		cm.setBackgroundColour("#f7f0e9");
		cm.setTooltipStyle(new ToolTip(MouseStyle.FOLLOW));    

		return cm;    
		
	}

}
