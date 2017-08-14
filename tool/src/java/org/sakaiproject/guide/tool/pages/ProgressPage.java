package org.sakaiproject.guide.tool.pages;

import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.color.ColorReference;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.color.HighchartsColor;
import com.googlecode.wickedcharts.highcharts.options.color.NullColor;
import com.googlecode.wickedcharts.highcharts.options.functions.PercentageFormatter;
import com.googlecode.wickedcharts.highcharts.options.functions.StackTotalFormatter;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.authoring.model.ComalatMetadata;
import org.sakaiproject.guide.model.UserGradingAverageData;
import org.sakaiproject.util.ResourceLoader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The progress page is used for visualization of the learning progress in the
 * learning path
 * 
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 *
 */

public class ProgressPage extends BasePage {
	public static final String GRAMMAR_COLOR_CODE="#A0FFA0";
	public static final String VOCABULARY_COLOR_CODE="#FFA0A0";
	public static final String READING_COLOR_CODE="#A0F0FF";
	public static final String LISTENING_COLOR_CODE="#FFFF88";
	public static final String LSP_COLOR_CODE="#C0C0C0";

	private transient ResourceLoader rl = new ResourceLoader( "org.sakaiproject.guide.tool.MyApplication" );

	//Link<Void> toThirdPageLink;
	Link<Void> archetypalAnalysisLink;

	public ProgressPage() {
		disableLink(secondLink);

		// link to third page
		// the i18n label for this is directly in the HTML
		/**
		toThirdPageLink = new Link<Void>("toThirdPageLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ProfilePage());
			}
		};

		add(toThirdPageLink);
		*/
		// archetypalAnalysisLink
		archetypalAnalysisLink = new Link<Void>("archetypalAnalysisLink") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ArchetypalAnalysisPage());
			}
		};
		archetypalAnalysisLink.add(new AttributeModifier("title", true, new ResourceModel("progress_page.link.archetypalAnalysisLink.tooltip")));
		add(archetypalAnalysisLink);

		//get the grade info
		String userId = sakaiProxy.getCurrentUserId();
		String language = comalatUtilities.getShortLanguageOfCurrentSite();
		UserGradingAverageData g = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "G");
		UserGradingAverageData v = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "V");
		UserGradingAverageData r = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "R");
		UserGradingAverageData l = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "L");

		//form the averages and the count of activities
		int gcount = g.getCount();
		int vcount = v.getCount();
		int rcount = r.getCount();
		int lcount = l.getCount();
		int totalCount = gcount+vcount+rcount+lcount;

		double gcountp = totalCount!=0?scaleValue(gcount/(double)totalCount):0;
		double vcountp = totalCount!=0?scaleValue(vcount/(double)totalCount):0;
		double rcountp = totalCount!=0?scaleValue(vcount/(double)totalCount):0;
		double lcountp = totalCount!=0?scaleValue(lcount/(double)totalCount):0;

		double gavg = g.getAverage();
		double vavg = v.getAverage();
		double ravg = r.getAverage();
		double lavg = l.getAverage();

		int count = 0;
		double wholeSum = 0.0;
		if (gavg!=-1) {
			wholeSum += gavg;
			count++;
		}
		else {
			gavg=0;
		}
		if (vavg!=-1) {
			wholeSum += vavg;
			count++;
		}
		else {
			vavg=0;
		}
		if (ravg!=-1) {
			wholeSum += ravg;
			count++;
		}
		else {
			ravg=0;
		}
		if (lavg!=-1) {
			wholeSum += lavg;
			count++;
		}
		else {
			lavg=0;
		}
		double wholeAvg = 0.0;
		if (count!=0) {
			wholeAvg = (wholeSum / count);
		}

		//Column chart 1
		Options columnChart = new Options();
		columnChart
				.setChartOptions(new ChartOptions()
						.setBorderColor(new HexColor("#000000"))
						.setBorderWidth(3)
						.setType(SeriesType.COLUMN)
						.setMarginTop(50)
						.setMarginRight(50)
						.setMarginBottom(100)
						.setMarginLeft(80));

		columnChart
				.setTitle(new Title(rl.getString("progress_page.chartTitle1")));

		columnChart
				.setxAxis(new Axis()
						.setCategories(
								rl.getString("progress_page.message1"),
								rl.getString("progress_page.grammar"),
								rl.getString("progress_page.vocabulary"),
								rl.getString("progress_page.reading"),
								rl.getString("progress_page.listening"))
						.setLabels(new Labels()
								.setRotation(-45)
								.setAlign(HorizontalAlignment.RIGHT)
								.setStyle(new CssStyle()
										.setProperty("font-size", "13px")
										.setProperty("font-family", "Verdana, sans-serif"))));

		columnChart
				.setyAxis(new Axis()
						.setMin(0)
						.setTitle(new Title("Grade (0-100)")));

		columnChart
				.setLegend(new Legend(Boolean.TRUE));

		String message6= rl.getString("progress_page.message6");
		columnChart
				.setTooltip(new Tooltip()
						.setFormatter(new Function()
								.setFunction("return '<b>'+ this.x +'</b><br/>'+ '"+message6+
										": '+ Highcharts.numberFormat(this.y, 1) + '.';")));


		columnChart
				.addSeries(new PointSeries()
						.addPoint(new Point().setY(scaleValue(wholeAvg)).setColor(new HexColor("#F4A460")))
						.addPoint(new Point().setY(scaleValue(gavg)).setColor(new HexColor(GRAMMAR_COLOR_CODE)))
						.addPoint(new Point().setY(scaleValue(vavg)).setColor(new HexColor(VOCABULARY_COLOR_CODE)))
						.addPoint(new Point().setY(scaleValue(ravg)).setColor(new HexColor(READING_COLOR_CODE)))
						.addPoint(new Point().setY(scaleValue(lavg)).setColor(new HexColor(LISTENING_COLOR_CODE)))
						.setName((count!=0?rl.getString("progress_page.message.competences"):
								rl.getString("progress_page.message.noassessedactivities")))
						.setDataLabels(new DataLabels()
								.setEnabled(Boolean.TRUE)
								.setRotation(-90)
								.setColor(new HexColor("#000000"))
								.setAlign(HorizontalAlignment.RIGHT)
								.setX(-3)
								.setY(10)
								.setFormatter(new Function()
										.setFunction(" return this.y;"))
								.setStyle(new CssStyle()
										.setProperty("font-size", "13px")
										.setProperty("font-family", "Verdana, sans-serif"))));

		add(new Chart("columnChart1", columnChart));

		Options pieChart = new Options();
		pieChart.setChartOptions(new ChartOptions()
				.setPlotBackgroundColor(new NullColor())
				.setBorderColor(new HexColor("#000000"))
				.setBorderWidth(3)
				.setPlotShadow(Boolean.FALSE));

		pieChart.setTitle(new Title(count!=0?rl.getString("progress_page.message7"):
				rl.getString("progress_page.message.noassessedactivities")));

		pieChart.setTooltip(new Tooltip()
				.setFormatter(new PercentageFormatter())
				.setPercentageDecimals(2));

		pieChart.setPlotOptions(new PlotOptionsChoice()
				.setPie(new PlotOptions()
						.setAllowPointSelect(Boolean.TRUE)
						.setCursor(Cursor.POINTER)
						.setDataLabels(new DataLabels()
								.setEnabled(Boolean.TRUE)
								.setColor(new HexColor("#000000"))
								.setConnectorColor(new HexColor("#000000"))
								.setFormatter(new PercentageFormatter()))));

		pieChart.addSeries(new PointSeries()
				.setType(SeriesType.PIE)
				.setName(rl.getString("progress_page.message8"))
				.addPoint(new Point(rl.getString("progress_page.grammar"), gcountp).setColor(new HexColor(GRAMMAR_COLOR_CODE)))
				.addPoint(new Point(rl.getString("progress_page.vocabulary"), vcountp).setColor(new HexColor(VOCABULARY_COLOR_CODE)))
				.addPoint(new Point(rl.getString("progress_page.reading"), rcountp).setColor(new HexColor(READING_COLOR_CODE)))
				.addPoint(new Point(rl.getString("progress_page.listening"), lcountp).setColor(new HexColor(LISTENING_COLOR_CODE))));
		add(new Chart("pieChart1", pieChart));


		int ngcount = projectLogic.getCountOfNonAssessedActivitiesByUserIdAndCompetence(userId, language, "G");
		int nvcount = projectLogic.getCountOfNonAssessedActivitiesByUserIdAndCompetence(userId, language, "V");
		int nrcount = projectLogic.getCountOfNonAssessedActivitiesByUserIdAndCompetence(userId, language, "R");
		int nlcount = projectLogic.getCountOfNonAssessedActivitiesByUserIdAndCompetence(userId, language, "L");
		int totalNCount = ngcount + nvcount + nrcount + nlcount;
		/*
		int gcount = g.getCount();
		int vcount = v.getCount();
		int rcount = r.getCount();
		int lcount = l.getCount();
		int totalCount = gcount+vcount+rcount+lcount;
		*/

		//Column chart 2
		Options columnChart2 = new Options();
		columnChart2
				.setChartOptions(new ChartOptions()
						.setBorderColor(new HexColor("#000000"))
						.setBorderWidth(3)
						.setType(SeriesType.COLUMN)
						.setMarginTop(50)
						.setMarginRight(50)
						.setMarginBottom(100)
						.setMarginLeft(80));

		columnChart2
				.setTitle(new Title(rl.getString("progress_page.title3")));

		columnChart2.
			setxAxis(new Axis()
				.setCategories(rl.getString("progress_page.chart.assessed"), rl.getString("progress_page.chart.non_assessed")));

		columnChart2.
				setyAxis(new Axis()
				.setMin(0)
				.setTitle(new Title(rl.getString("progress_page.chart.number_of_activities")))
				.setStackLabels(new StackLabels()
						.setEnabled(Boolean.TRUE)
						.setStyle(new CssStyle()
								.setProperty("font-weight", "bold")
								.setProperty("color", "gray"))));

		columnChart2.setLegend(new Legend()
				.setAlign(HorizontalAlignment.RIGHT)
				.setX(-100)
				.setVerticalAlign(VerticalAlignment.TOP)
				.setY(20)
				.setFloating(Boolean.TRUE)
				.setBackgroundColor(new HexColor("#FFFFFF"))
				.setBorderColor(new HexColor("#CCCCCC"))
				.setBorderWidth(1)
				.setShadow(Boolean.FALSE));

		columnChart2.setTooltip(new Tooltip()
				.setFormatter(new StackTotalFormatter()));

		columnChart2.setPlotOptions(new PlotOptionsChoice()
				.setColumn(new PlotOptions()
						.setStacking(Stacking.NORMAL)
						.setDataLabels(new DataLabels()
								.setEnabled(Boolean.TRUE)
								.setColor(new HexColor("#FFFFFF")))));

		columnChart2.addSeries(new SimpleSeries()
				.setName(rl.getString("progress_page.grammar"))
				.setData(gcount,ngcount)
				.setColor(new HexColor(GRAMMAR_COLOR_CODE)));

		columnChart2.addSeries(new SimpleSeries()
				.setName(rl.getString("progress_page.vocabulary"))
				.setData(vcount,nvcount)
				.setColor(new HexColor(VOCABULARY_COLOR_CODE)));

		columnChart2.addSeries(new SimpleSeries()
				.setName(rl.getString("progress_page.listening"))
				.setData(lcount,nlcount)
				.setColor(new HexColor(LISTENING_COLOR_CODE)));

		columnChart2.addSeries(new SimpleSeries()
				.setName(rl.getString("progress_page.reading"))
				.setData(rcount,nrcount)
				.setColor(new HexColor(READING_COLOR_CODE)));

		add(new Chart("columnChart2", columnChart2));

		//bar char with topics average grades in assessed activities for topics
		Options barChart1 = new Options();

		barChart1.setChartOptions(new ChartOptions()
				.setType(SeriesType.BAR));

		barChart1.setGlobal(new Global()
				.setUseUTC(Boolean.TRUE));

		barChart1.setTitle(new Title(rl.getString("progress_page.barchart.barchart1_avg_title_message")));

		//barChart1.setSubtitle(new Title("blah blah blah"));

		//get the available metadata tags for the current language
		List<ComalatMetadata> metadata = projectLogic.getComalatMetadataForLanguage(language);
		//for each one of them get the current user average in this metadata tag
		List<UserGradingAverageData> data = new ArrayList<>();
		List<String> categories = new ArrayList<>();
		List<Number> averages = new ArrayList<>();
		for (ComalatMetadata meta : metadata) {
			UserGradingAverageData dataItem = projectLogic.getAssessedAvgByUserIdAndMetadata(
					userId,language,meta.getMetadataTag());
			if (dataItem.getCount()!=0) {
				data.add(dataItem);
				categories.add(meta.getMetadataName());
				averages.add(scaleValue(dataItem.getAverage()));
			}
		}


		barChart1.setxAxis(new Axis()
				.setCategories(categories)
				.setTitle(new Title(null)));

		barChart1.setyAxis(new Axis()
				.setTitle(
						new Title(rl.getString("progress_page.barchart.barchart1_subtitle_message"))
								.setAlign(HorizontalAlignment.HIGH))
				.setLabels(new Labels().setOverflow(Overflow.JUSTIFY)));

		barChart1.setTooltip(new Tooltip()
				.setFormatter(new Function(
						"return ''+this.series.name +': '+ this.y +'.';")));

		barChart1.setPlotOptions(new PlotOptionsChoice()
				.setBar(new PlotOptions()
						.setDataLabels(new DataLabels()
								.setEnabled(Boolean.TRUE))));

		barChart1.setLegend(new Legend()
				.setLayout(LegendLayout.VERTICAL)
				.setAlign(HorizontalAlignment.RIGHT)
				.setVerticalAlign(VerticalAlignment.TOP)
				.setX(-100)
				.setY(100)
				.setFloating(Boolean.TRUE)
				.setBorderWidth(1)
				.setBackgroundColor(new HexColor("#ffffff"))
				.setShadow(Boolean.TRUE));

		barChart1.setCredits(new CreditOptions()
				.setEnabled(Boolean.FALSE));

		barChart1.addSeries(new SimpleSeries()
				.setName(rl.getString("progress_page.barchart.barchart1_average_grade"))
				.setData(averages));

		add(new Chart("barChart1", barChart1));

		//bar chart with LSPs average grades in assessed activities for LSPs
		Options barChart2 = new Options();

		barChart2.setChartOptions(new ChartOptions()
				.setType(SeriesType.BAR));

		barChart2.setGlobal(new Global()
				.setUseUTC(Boolean.TRUE));

		barChart2.setTitle(new Title(rl.getString("progress_page.barchart.barchart2_avg_title_message")));

		//barChart1.setSubtitle(new Title("blah blah blah"));

		/**
		 * for each LSP tag
		 * LSPB (Business and Professional Language)
		 * LSPH (Health)
		 * LSPS (Science and Technology)
		 * LSPT (Tourism and Hospitality)
		 * get the current user average in this metadata tag
		 */

		UserGradingAverageData LSPB = projectLogic.getAssessedAvgByUserIdAndMetadata(
					userId,language,"LSPB");

		UserGradingAverageData LSPH = projectLogic.getAssessedAvgByUserIdAndMetadata(
				userId,language,"LSPH");

		UserGradingAverageData LSPS = projectLogic.getAssessedAvgByUserIdAndMetadata(
				userId,language,"LSPS");

		UserGradingAverageData LSPT = projectLogic.getAssessedAvgByUserIdAndMetadata(
				userId,language,"LSPT");

		barChart2.setxAxis(new Axis()
				.setCategories(rl.getString("progress_page.barchart.barchart2_LSPB"),
						rl.getString("progress_page.barchart.barchart2_health"),
						rl.getString("progress_page.barchart.barchart2_science"),
						rl.getString("progress_page.barchart.barchart2_tourism"))
				.setTitle(new Title(null)));

		barChart2.setyAxis(new Axis()
				.setTitle(
						new Title(rl.getString("progress_page.barchart.barchart1_subtitle_message"))
								.setAlign(HorizontalAlignment.HIGH))
				.setLabels(new Labels().setOverflow(Overflow.JUSTIFY)));

		barChart2.setTooltip(new Tooltip()
				.setFormatter(new Function(
						"return ''+this.series.name +': '+ this.y +'.';")));

		barChart2.setPlotOptions(new PlotOptionsChoice()
				.setBar(new PlotOptions()
						.setDataLabels(new DataLabels()
								.setEnabled(Boolean.TRUE))));

		barChart2.setLegend(new Legend()
				.setLayout(LegendLayout.VERTICAL)
				.setAlign(HorizontalAlignment.RIGHT)
				.setVerticalAlign(VerticalAlignment.TOP)
				.setX(-100)
				.setY(100)
				.setFloating(Boolean.TRUE)
				.setBorderWidth(1)
				.setBackgroundColor(new HexColor("#ffffff"))
				.setShadow(Boolean.TRUE));

		barChart2.setCredits(new CreditOptions()
				.setEnabled(Boolean.FALSE));

		barChart2.addSeries(new SimpleSeries()
				.setName(rl.getString("progress_page.barchart.barchart1_average_grade"))
				.setData(LSPB.getCount()!=0?scaleValue(LSPB.getAverage()):0,
						LSPH.getCount()!=0?scaleValue(LSPH.getAverage()):0,
						LSPS.getCount()!=0?scaleValue(LSPS.getAverage()):0,
						LSPT.getCount()!=0?scaleValue(LSPT.getAverage()):0));

		add(new Chart("barChart2", barChart2));
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		if (rl==null) {
			rl = new ResourceLoader( "org.sakaiproject.guide.tool.MyApplication" );
		}
	}

	private double scaleValue(double value) {
		value *= 100;
		BigDecimal bigDecimal = new BigDecimal(value);
		BigDecimal roundedWithScale = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		double scaled = roundedWithScale.doubleValue();
		return scaled;
	}

	/**
	 * Helper data structure to store usage data of a browser.
	 */
	public class BrowserUsageData {

		private static final long serialVersionUID = 1L;

		private String browserName;

		private Float marketShare;

		private ColorReference color;

		private List<VersionUsageData> versionUsageData = new ArrayList<VersionUsageData>();

		public String getBrowserName() {
			return this.browserName;
		}

		public ColorReference getColor() {
			return this.color;
		}

		public Float getMarketShare() {
			return this.marketShare;
		}

		public List<VersionUsageData> getVersionUsageData() {
			return this.versionUsageData;
		}

		public void setBrowserName(final String browserName) {
			this.browserName = browserName;
		}

		public void setColor(final ColorReference color) {
			this.color = color;
		}

		public void setMarketShare(final Float marketShare) {
			this.marketShare = marketShare;
		}

		public void setVersionUsageData(final List<VersionUsageData> versionUsageData) {
			this.versionUsageData = versionUsageData;
		}
	}

	/**
	 * Helper data structure to store usage data of a browser version.
	 */
	public class VersionUsageData {

		private final String name;

		private final Float marketShare;

		private final ColorReference color;

		public VersionUsageData(final String name, final Float marketShare, final ColorReference color) {
			this.name = name;
			this.marketShare = marketShare;
			this.color = color;
		}

		public ColorReference getColor() {
			return this.color;
		}

		public Float getMarketShare() {
			return this.marketShare;
		}

		public String getName() {
			return this.name;
		}
	}





	/**
	 * Creates the data displayed in the donut chart.
	 */
	private List<BrowserUsageData> getBrowserData() {
		List<BrowserUsageData> browserData = new ArrayList<BrowserUsageData>();
		browserData.add(getMSIEUsageData());
		browserData.add(getOperaUsageData());
		return browserData;
	}

	/**
	 * Creates the Internet Explorer data.
	 */
	private BrowserUsageData getMSIEUsageData() {
		BrowserUsageData data = new BrowserUsageData();
		data.setBrowserName("MSIE");
		data.setMarketShare(55.11f);
		ColorReference ieColor = new HighchartsColor(0);
		data.setColor(ieColor);
		data.getVersionUsageData().add(new VersionUsageData("Vocabulary", 10.85f, ieColor.brighten(0.1f)));
		data.getVersionUsageData().add(new VersionUsageData("Writing", 7.35f, ieColor.brighten(0.2f)));
		data.getVersionUsageData().add(new VersionUsageData("Listening", 33.06f, ieColor.brighten(0.3f)));
		data.getVersionUsageData().add(new VersionUsageData("Reading", 2.81f, ieColor.brighten(0.4f)));
		return data;
	}

	/**
	 * Creates the Opera data.
	 */
	private BrowserUsageData getOperaUsageData() {
		BrowserUsageData data = new BrowserUsageData();
		data.setBrowserName("Opera");
		data.setMarketShare(2.14f);
		ColorReference operaColor = new HighchartsColor(4);
		data.setColor(operaColor);
		data.getVersionUsageData()
				.add(new VersionUsageData("Language for specific purposes", 10.12f, operaColor.brighten(0.1f)));
		data.getVersionUsageData().add(new VersionUsageData("Oral Production", 30.37f, operaColor.brighten(0.2f)));
		data.getVersionUsageData().add(new VersionUsageData("Oral Interaction", 51.65f, operaColor.brighten(0.3f)));
		return data;
	}

	/**
	 * Converts a list of {@link BrowserUsageData} into a list of
	 * {@link PointSeries} containing the data about the browsers.
	 */
	private PointSeries toBrowserSeries(final List<BrowserUsageData> browserUsage) {
		PointSeries browserSeries = new PointSeries();
		for (BrowserUsageData browserData : browserUsage) {
			browserSeries.addPoint(
					new Point(browserData.getBrowserName(), browserData.getMarketShare(), browserData.getColor()));
		}
		return browserSeries;
	}

	/**
	 * Converts a list of {@link BrowserUsageData} into a list of
	 * {@link PointSeries} containing the data about the browser versions.
	 */
	private PointSeries toVersionSeries(final List<BrowserUsageData> browserUsage) {
		PointSeries versionSeries = new PointSeries();
		for (BrowserUsageData browserData : browserUsage) {
			for (VersionUsageData versionData : browserData.getVersionUsageData()) {
				versionSeries.addPoint(
						new Point(versionData.getName(), versionData.getMarketShare(), versionData.getColor()));
			}
		}
		return versionSeries;
	}



}
