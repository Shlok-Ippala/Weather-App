package com.aurora.climatesync.view.component;

import com.aurora.climatesync.model.WeatherForecast;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A Google-style weather chart panel displaying temperature trends and precipitation.
 * Shows high/low temperatures as lines and precipitation probability as bars.
 */
public class WeatherChartPanel extends JPanel {
    
    private static final Color TEMP_HIGH_COLOR = new Color(255, 107, 107);  // Coral red
    private static final Color TEMP_LOW_COLOR = new Color(78, 205, 196);    // Teal
    private static final Color PRECIP_COLOR = new Color(100, 149, 237, 150); // Cornflower blue with transparency
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 252);
    private static final Color GRID_COLOR = new Color(230, 230, 235);
    
    private ChartPanel chartPanel;
    
    public WeatherChartPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create empty chart initially
        JFreeChart chart = createEmptyChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 250));
        chartPanel.setBackground(BACKGROUND_COLOR);
        chartPanel.setBorder(null);
        
        add(chartPanel, BorderLayout.CENTER);
    }
    
    /**
     * Updates the chart with new weather forecast data.
     */
    public void updateChart(List<WeatherForecast> forecasts) {
        if (forecasts == null || forecasts.isEmpty()) {
            chartPanel.setChart(createEmptyChart());
            return;
        }
        
        // Create datasets
        DefaultCategoryDataset tempDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset precipDataset = new DefaultCategoryDataset();
        
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("EEE M/d");
        
        for (WeatherForecast forecast : forecasts) {
            String label = forecast.getDate().format(labelFormatter);
            
            tempDataset.addValue(forecast.getMaxTemperature(), "High", label);
            tempDataset.addValue(forecast.getMinTemperature(), "Low", label);
            precipDataset.addValue(forecast.getPrecipitationChance() * 100, "Precipitation", label);
        }
        
        // Create the chart
        JFreeChart chart = createChart(tempDataset, precipDataset);
        chartPanel.setChart(chart);
    }
    
    private JFreeChart createEmptyChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createLineChart(
                null,           // No title
                null,           // No x-axis label
                "Temperature (°C)", 
                dataset,
                PlotOrientation.VERTICAL,
                false,          // No legend initially
                true,           // Tooltips
                false           // No URLs
        );
        styleChart(chart);
        return chart;
    }
    
    private JFreeChart createChart(DefaultCategoryDataset tempDataset, 
                                   DefaultCategoryDataset precipDataset) {
        
        // Create base chart with temperature data
        JFreeChart chart = ChartFactory.createLineChart(
                null,                    // No title (we'll add our own header)
                null,                    // No x-axis label
                "Temperature (°C)",
                tempDataset,
                PlotOrientation.VERTICAL,
                true,                    // Show legend
                true,                    // Tooltips
                false                    // No URLs
        );
        
        CategoryPlot plot = chart.getCategoryPlot();
        
        // Style the temperature line renderer
        LineAndShapeRenderer lineRenderer = (LineAndShapeRenderer) plot.getRenderer();
        lineRenderer.setSeriesPaint(0, TEMP_HIGH_COLOR);  // High temp
        lineRenderer.setSeriesPaint(1, TEMP_LOW_COLOR);   // Low temp
        lineRenderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        lineRenderer.setSeriesStroke(1, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        lineRenderer.setSeriesShapesVisible(0, true);
        lineRenderer.setSeriesShapesVisible(1, true);
        lineRenderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10));
        lineRenderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10));
        lineRenderer.setSeriesShapesFilled(0, true);
        lineRenderer.setSeriesShapesFilled(1, true);
        
        // Add precipitation bars on secondary axis
        NumberAxis precipAxis = new NumberAxis("Precipitation (%)");
        precipAxis.setRange(0, 100);
        precipAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        precipAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        precipAxis.setTickLabelPaint(PRECIP_COLOR.darker());
        precipAxis.setLabelPaint(PRECIP_COLOR.darker());
        plot.setRangeAxis(1, precipAxis);
        plot.setDataset(1, precipDataset);
        plot.mapDatasetToRangeAxis(1, 1);
        
        // Create bar renderer for precipitation
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setSeriesPaint(0, PRECIP_COLOR);
        barRenderer.setMaximumBarWidth(0.08);
        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);
        plot.setRenderer(1, barRenderer);
        
        // Render bars behind lines
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        
        // Style the chart
        styleChart(chart);
        
        return chart;
    }
    
    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(BACKGROUND_COLOR);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(BACKGROUND_COLOR);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        // Style domain axis (days)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setLowerMargin(0.05);
        domainAxis.setUpperMargin(0.05);
        domainAxis.setMaximumCategoryLabelLines(1);
        domainAxis.setMaximumCategoryLabelWidthRatio(0.8f);
        
        // Style range axis (temperature)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setAutoRangeIncludesZero(false);
        
        // Add some padding to temperature range
        rangeAxis.setAutoRangeStickyZero(false);
    }
}

