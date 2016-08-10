package er.bugtracker.components.reporting;

import java.text.Format;
import java.util.Enumeration;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXDictionaryUtilities;
import er.grouping.DRCriteria;
import er.grouping.DRGroup;
import er.grouping.DRRecordGroup;
import er.grouping.DRSubMasterCriteria;
import er.grouping.DRValue;
import er.plot.ERPCategoryChart;
import er.plot.ERPPieChart;


public class ChartReport extends Report {
    public String currentType;
    
    public ChartConfiguration selectedConfiguration = (ChartConfiguration) CHART_CONFIGURATIONS.lastObject();
    public ChartConfiguration currentConfiguration;
    public String selectedType = selectedConfiguration.defaultType();

    private static NSArray CHART_CONFIGURATIONS = new NSArray(new Object[] {
            new PieChartConfiguration(),
            new AreaChartConfiguration()
    });
    
    public NSArray chartConfigurations() {
        return CHART_CONFIGURATIONS;
    }
    
    public static interface ChartConfiguration {
        
        public  NSArray supportedTypes();
        public  NSDictionary configuration();
        public  String componentName();
        public  Dataset createDataset();
        public  String defaultType();
    }
    
    public static class PieChartConfiguration implements ChartConfiguration {
        
        public NSArray supportedTypes() {
            return ERPPieChart.SUPPORTED_TYPES;
        }
        
        public NSDictionary configuration() {
            return null;
        }
        
        public String componentName() {
            return ERPPieChart.class.getName();
        }
        
        public Dataset createDataset() {
            return new ERPPieChart.AccumulatingPieDataset();
        }
        
        public String defaultType() {
            return "PieChart";
        }
       
    }
    public static class AreaChartConfiguration implements ChartConfiguration {
        
        public NSArray supportedTypes() {
            return ERPCategoryChart.SUPPORTED_TYPES;
        }
        
        public NSDictionary configuration() {
            return  ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(new Object[] {
                    CategoryLabelPositions.UP_45, "categoryPlot.domainAxis.categoryLabelPositions"
            });
        }
        
        public String componentName() {
            return ERPCategoryChart.class.getName();
        }
        
        public Dataset createDataset() {
            return new DefaultCategoryDataset();
        }
        
        public String defaultType() {
            return "StackedAreaChart";
        }
   }
    
    /**
     * Public constructor
     * @param context the context
     */
    public ChartReport(WOContext context) {
        super(context);
    }
    
    @Override
    public void awake() {
        super.awake();
    }

    public boolean isCurrentTypeSelected() {
        return currentType == selectedType;
    }
    
    public void selectType() {
        selectedType = currentType;
        selectedConfiguration = currentConfiguration;
    }
    
    public JFreeChart chart() {
        // NOTE: AK this is a dummy implementation, because ERPChart.chart will setValueForBinding("chart"), which will
        // try to validate the binding, which in turn calls valueForKey("chart"), which would finally choke and call
        // handleQueryForUnboundKey if we didn't return null here. Duh.
        return null;
    }
    
    public void setChart(JFreeChart chart) {
        // chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions();
    }
    
    public Dataset dataset(Integer attribTotalKey) {
        Dataset dataset = selectedConfiguration.createDataset();
        NSArray dataList = model().vList();
        NSArray valueList = model().hList();
        if(true) {
            dataList = model().hList();
            valueList = model().vList();
        }
        DRGroup dayGroup =  (DRGroup) dataList.objectAtIndex(0);
        DRSubMasterCriteria daySubMasterCriteria = (DRSubMasterCriteria) dayGroup.masterCriteria().subCriteriaList().objectAtIndex(0);
        String dayKey = daySubMasterCriteria.keyDesc();
        NSArray dayCriterias = dayGroup.sortedCriteriaList();
        Format dayFormat = DRCriteria.formatterForFormat(daySubMasterCriteria.format());
        
        NSMutableDictionary coordinates = new NSMutableDictionary();
        for(Enumeration en = valueList.objectEnumerator(); en.hasMoreElements();) {
            
            DRGroup mainGroup = (DRGroup)en.nextElement();
            DRSubMasterCriteria mainSubMasterCriteria = (DRSubMasterCriteria) mainGroup.masterCriteria().subCriteriaList().lastObject();
            String mainKey = mainSubMasterCriteria.keyDesc();
            NSArray mainCriterias = mainGroup.sortedCriteriaList();

            for (Enumeration values = mainCriterias.objectEnumerator(); values.hasMoreElements();) {
                DRCriteria mainCriteria = (DRCriteria)values.nextElement();
                String label = (String) mainCriteria.valueDict().objectForKey(mainKey);
                coordinates.setObjectForKey(mainCriteria, mainGroup.masterCriteria().keyDesc());
                if(!mainCriteria.isTotal()) {
                    for (Enumeration days = dayCriterias.objectEnumerator(); days.hasMoreElements();) {
                        DRCriteria dayCriteria = (DRCriteria)days.nextElement();
                        Object o = dayCriteria.valueDict().objectForKey(dayKey);
                        if(!dayCriteria.isTotal()) {
                            // we just want the actual times, not the totals
                            if(o instanceof NSTimestamp) {
                                NSTimestamp ts = (NSTimestamp)o;
                                if(ts != null) {
                                    double value = 0.0D;
                                    String dayLabel = dayFormat.format(ts);
                                    coordinates.setObjectForKey(dayCriteria, dayGroup.masterCriteria().keyDesc());
                                    DRRecordGroup recordGroup = model().recordGroupForCoordinates(coordinates);
                                    if(attribTotalKey != null) {
                                        DRValue drValue = (DRValue) recordGroup.totals().objectForKey(attribTotalKey);
                                        if(drValue != null) {
                                            value = drValue.total();
                                        }
                                    } else {
                                        value = ((Number) recordGroup.rawRecordList().valueForKeyPath(recordGroupTotalToShow())).doubleValue();
                                    }
                                    if(dataset instanceof CategoryDataset) {
                                        ((DefaultCategoryDataset)dataset).setValue(value, label, dayLabel);
                                    } else if (dataset instanceof PieDataset) {
                                        ((DefaultPieDataset) dataset).setValue(label, value);
                                    } else {
                                        throw new IllegalArgumentException("Neither a Pie nor CategoryDataset");
                                    }
                                }
                            } else {
                                double value = 0.0D;
                                coordinates.setObjectForKey(dayCriteria, dayGroup.masterCriteria().keyDesc());
                                DRRecordGroup recordGroup = model().recordGroupForCoordinates(coordinates);
                                if(attribTotalKey != null) {
                                    DRValue drValue = (DRValue) recordGroup.totals().objectForKey(attribTotalKey);
                                    if(drValue != null) {
                                        value = drValue.total();
                                    }
                                } else {
                                    value = ((Number) recordGroup.rawRecordList().valueForKeyPath(recordGroupTotalToShow())).doubleValue();
                                }
                                if(dataset instanceof CategoryDataset) {
                                    ((DefaultCategoryDataset)dataset).setValue(value, label, o.toString());
                                } else if (dataset instanceof PieDataset) {
                                    ((DefaultPieDataset) dataset).setValue(label, value);
                                } else {
                                    throw new IllegalArgumentException("Neither a Pie nor CategoryDataset");
                                }
                            }
                        }
                    }
                }
            }
        }
       return dataset;
    }
    
    public Dataset datasetForAttribute() {
        Integer attribTotalKey = Integer.valueOf(model().flatAttributeList().indexOfObject(attrib));
        return dataset(attribTotalKey);
    }
    
    public Dataset dataset() {
        return dataset(null);
    }
}
