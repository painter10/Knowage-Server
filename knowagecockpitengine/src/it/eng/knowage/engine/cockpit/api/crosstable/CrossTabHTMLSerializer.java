/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.cockpit.api.crosstable;

import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.CellType;
import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.MeasureInfo;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabDefinition.Column;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabDefinition.Row;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CrossTabHTMLSerializer {

	private static String TABLE_TAG = "TABLE";
	private static String ROW_TAG = "TR";
	private static String COLUMN_TAG = "TD";
	private static String ICON_TAG = "I";
	private static String COLUMN_DIV = "DIV";
	private static String CLASS_ATTRIBUTE = "class";
	private static String STYLE_ATTRIBUTE = "style";
	private static String ROWSPAN_ATTRIBUTE = "rowspan";
	private static String COLSPAN_ATTRIBUTE = "colspan";
	private static final String ON_CLICK_ATTRIBUTE = "onClick";
	private static final String NG_CLICK_ATTRIBUTE = "ng-click";

	private static String EMPTY_CLASS = "empty";
	private static String MEMBER_CLASS = "member";
	private static String LEVEL_CLASS = "level";
	private static String NA_CLASS = "na";

	private static String DEFAULT_BG_TOTALS = "background:rgba(59, 103, 140, 0.45);";
	private static String DEFAULT_BG_SUBTOTALS = "background:rgba(59, 103, 140, 0.8);";
	private static String DEFAULT_COLOR_TOTALS = "white;";

	private Locale locale = null;
	private final Integer myGlobalId;
	private final Map<Integer, NodeComparator> columnsSortKeysMap;
	private final Map<Integer, NodeComparator> rowsSortKeysMap;

	private static Logger logger = Logger.getLogger(CrossTabHTMLSerializer.class);

	public CrossTabHTMLSerializer(Locale locale, Integer myGlobalId, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap) {
		this.columnsSortKeysMap = columnsSortKeysMap;
		this.rowsSortKeysMap = rowsSortKeysMap;
		this.locale = locale;
		this.myGlobalId = myGlobalId;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;

	}

	public String serialize(CrossTab crossTab) {
		logger.debug("IN");
		String html = null;
		try {
			SourceBean sb = this.getSourceBean(crossTab);
			html = sb.toXML(false);
		} catch (Exception e) {
			logger.error("Error while serializing crossTab", e);
			throw new SpagoBIEngineRuntimeException("Error while serializing crossTab", e);
		}
		LogMF.debug(logger, "OUT : returning {0}", html);
		return html;
	}

	private SourceBean getSourceBean(CrossTab crossTab) throws SourceBeanException, JSONException {

		SourceBean emptyTopLeftCorner = this.serializeTopLeftCorner(crossTab);
		SourceBean rowsHeaders = this.serializeRowsHeaders(crossTab);
		SourceBean topLeftCorner = this.mergeVertically(emptyTopLeftCorner, rowsHeaders);
		SourceBean columnsHeaders = this.serializeColumnsHeaders(crossTab);
		SourceBean head = this.mergeHorizontally(topLeftCorner, columnsHeaders);

		SourceBean rowsMember = this.serializeRowsMembers(crossTab);
		SourceBean data = this.serializeData(crossTab);
		SourceBean body = this.mergeHorizontally(rowsMember, data);

		SourceBean crossTabSB = this.mergeVertically(head, body);

		return crossTabSB;
	}

	private SourceBean serializeRowsMembers(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		int leaves = crossTab.getRowsRoot().getLeafsNumber();

		if (leaves == 1) { // only root node exists
			// no attributes on rows, maybe measures?
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
				for (int i = 0; i < measures.size(); i++) {
					Measure measure = measures.get(i);
					SourceBean aRow = new SourceBean(ROW_TAG);
					SourceBean aColumn = new SourceBean(COLUMN_TAG);
					aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
					String measureAlias = measure.getAlias();
					String text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
					aColumn.setCharacters(text);
					aRow.setAttribute(aColumn);
					table.setAttribute(aRow);
				}
			} else {
				// nothing on rows
				SourceBean aRow = new SourceBean(ROW_TAG);
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
				aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.data", this.getLocale()));
				aRow.setAttribute(aColumn);
				table.setAttribute(aRow);
				JSONObject config = crossTab.getCrosstabDefinition().getConfig();
				String rowsTotals = config.optString("calculatetotalsoncolumns");
				if (rowsTotals != null && rowsTotals.equals("on")) {
					SourceBean totalRow = new SourceBean(ROW_TAG);
					SourceBean totalColumn = new SourceBean(COLUMN_TAG);
					totalColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
					totalColumn.setCharacters(CrossTab.TOTAL);
					totalRow.setAttribute(totalColumn);
					table.setAttribute(totalRow);
				}
			}
		} else {
			List<SourceBean> rows = new ArrayList<SourceBean>();
			// initialize all rows (with no columns)
			for (int i = 0; i < leaves; i++) {
				SourceBean aRow = new SourceBean(ROW_TAG);
				table.setAttribute(aRow);
				rows.add(aRow);
			}

			int levels = crossTab.getRowsRoot().getDistanceFromLeaves();
			if (crossTab.isMeasureOnRow()) {
				levels--;
			}
			boolean addedLabelTotal = false;
			for (int i = 0; i < levels; i++) {
				List<Node> levelNodes = crossTab.getRowsRoot().getLevel(i + 1);
				int counter = 0;
				for (int j = 0; j < levelNodes.size(); j++) {
					SourceBean aRow = rows.get(counter);
					Node aNode = levelNodes.get(j);
					SourceBean aColumn = new SourceBean(COLUMN_TAG);

					// Get specific columns configuration (format, bgcolor, icon visualization,..)
					String style;
					boolean appliedStyle = false;
					List<Row> rowsDef = crossTab.getCrosstabDefinition().getRows();
					Row row = rowsDef.get(i);

					JSONObject rowConfig = row.getConfig();
					style = getConfiguratedElementStyle(null, null, rowConfig, crossTab);
					if (!style.equals("")) {
						style += " padding:0;";
						aColumn.setAttribute(STYLE_ATTRIBUTE, style);
						appliedStyle = true;
					}
					if (!appliedStyle)
						aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);

					String text = null;
					if (crossTab.getCrosstabDefinition().isMeasuresOnRows() && i + 1 == levels) {
						String measureAlias = aNode.getDescription();
						text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
						// } else if (crossTab.getCrosstabDefinition().isMeasuresOnRows() && crossTab.getCrosstabDefinition().getMeasures().size() == 1) {
						// text = "";
					} else {
						text = aNode.getDescription();
						if (text.equalsIgnoreCase("Total")) {
							if (addedLabelTotal)
								text = "";
							else
								addedLabelTotal = true;
						}
						// else if (crossTab.getCrosstabDefinition().getMeasures().size() == 1)
						// text = "";

					}

					aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "clickFunction('" + crossTab.getCrosstabDefinition().getRows().get(i).getEntityId() + "','" + text
							+ "')");
					aColumn.setCharacters(text);
					int rowSpan = aNode.getLeafsNumber();
					if (rowSpan > 1) {
						aColumn.setAttribute(ROWSPAN_ATTRIBUTE, rowSpan);
					}
					aRow.setAttribute(aColumn);
					counter = counter + rowSpan;
				}
			}
		}

		return table;
	}

	private SourceBean serializeColumnsHeaders(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String parentStyle = null;

		int levels = crossTab.getColumnsRoot().getDistanceFromLeaves();
		if (levels == 0) {
			// nothing on columns
			SourceBean aRow = new SourceBean(ROW_TAG);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			aColumn.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
			aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.data", this.getLocale()));
			aRow.setAttribute(aColumn);
			table.setAttribute(aRow);
		} else {
			for (int i = 0; i < levels; i++) {
				boolean showHeader = true;
				SourceBean aRow = new SourceBean(ROW_TAG);
				List<Node> levelNodes = crossTab.getColumnsRoot().getLevel(i + 1);
				for (int j = 0; j < levelNodes.size(); j++) {
					Node aNode = levelNodes.get(j);
					SourceBean aColumn = new SourceBean(COLUMN_TAG);
					// odd levels are levels (except the last one, since it
					// contains measures' names)
					boolean isLevel = !((i + 1) % 2 == 0 || (i + 1) == levels);

					String className = !isLevel ? MEMBER_CLASS : LEVEL_CLASS;
					aColumn.setAttribute(CLASS_ATTRIBUTE, className);

					String text = null;
					String style = "";
					if (crossTab.getCrosstabDefinition().isMeasuresOnColumns() && i + 1 == levels) {
						String measureAlias = aNode.getDescription();
						text = MeasureScaleFactorOption.getScaledName(measureAlias, crossTab.getMeasureScaleFactor(measureAlias), this.locale);
						// check header visibility for measures
						List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
						if (measures.size() == 1) {
							Measure measure = measures.get(0);
							JSONObject measureConfig = measure.getConfig();
							if (!measureConfig.isNull("showHeader"))
								showHeader = measureConfig.getBoolean("showHeader");
						} else
							// for default with 2 or more measures the header is always visible
							showHeader = true;
					} else {
						// categories headers
						text = aNode.getDescription();
						// Get specific columns configuration (format, bgcolor, icon visualization,..)
						List<Column> columns = crossTab.getCrosstabDefinition().getColumns();
						for (int c = 0; c < columns.size(); c++) {
							Column col = columns.get(c);
							if (col.getAlias().equals(text)) {
								JSONObject columnConfig = col.getConfig();
								if (isLevel && !columnConfig.isNull("showHeader"))
									showHeader = columnConfig.getBoolean("showHeader");
								style = getConfiguratedElementStyle(null, null, columnConfig, crossTab);
								if (!style.equals("")) {
									style += " padding:0;";
									aColumn.setAttribute(STYLE_ATTRIBUTE, style);
									parentStyle = style;
									break;
								}
							}
						}
					}

					if (isLevel) {
						aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "orderPivotTable('" + i + "','1'," + myGlobalId + ")");
						// aColumn.setAttribute(ON_CLICK_ATTRIBUTE, "javascript:Sbi.cockpit.widgets.crosstab.HTMLCrossTab.sort('" + i + "','1'," + myGlobalId+
						// ")");

						Integer direction = 1;
						if (columnsSortKeysMap != null && columnsSortKeysMap.get(i) != null) {
							direction = columnsSortKeysMap.get(i).getDirection();
						}
						if (parentStyle != null)
							style = parentStyle;
						aColumn.setAttribute(addSortArrow(aRow, text, style, direction));
						aColumn.setAttribute(STYLE_ATTRIBUTE, style);

					} else {
						aColumn.setCharacters(text);
						boolean parentIsLevel = !((i) % 2 == 0 || (i) == levels);
						if (parentIsLevel) {
							aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "clickFunction('" + crossTab.getColumnsRoot().getLevel(i).get(0).getValue() + "','" + text
									+ "')");
							// Set the parent node style
							if (parentStyle != null && !parentStyle.equals("")) {
								aColumn.setAttribute(STYLE_ATTRIBUTE, parentStyle);
							}
						} else {
							// Measures Headers: Get parent node for get correct configuration
							if (parentStyle != null && !parentStyle.equals("")) {
								aColumn.setAttribute(STYLE_ATTRIBUTE, parentStyle);
							}
						}
					}

					int colSpan = aNode.getLeafsNumber();
					if (colSpan > 1) {
						aColumn.setAttribute(COLSPAN_ATTRIBUTE, colSpan);
					}
					aRow.setAttribute(aColumn);
				}
				if (showHeader)
					table.setAttribute(aRow);
			}
		}
		return table;
	}

	private SourceBean serializeData(CrossTab crossTab) throws SourceBeanException, JSONException {
		SourceBean table = new SourceBean(TABLE_TAG);
		String[][] data = crossTab.getDataMatrix();
		List<MeasureInfo> measuresInfo = crossTab.getMeasures();

		List<SourceBean> measureHeaders = new ArrayList<SourceBean>();
		if (crossTab.isMeasureOnRow()) {
			for (MeasureInfo measureInfo : crossTab.getMeasures()) {
				SourceBean aMeasureHeader = new SourceBean(COLUMN_TAG);
				// Get specific columns configuration (format, bgcolor, icon visualization,..)
				List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
				for (int m = 0; m < measures.size(); m++) {
					Measure measure = measures.get(m);
					if (measure.getAlias().equals(measureInfo.getName())) {
						JSONObject measureConfig = measure.getConfig();
						String style = getConfiguratedElementStyle(null, null, measureConfig, crossTab);
						if (!style.equals("")) {
							aMeasureHeader.setAttribute(STYLE_ATTRIBUTE, style);
							// appliedStyle = true;
							break;
						}
					}
				}
				aMeasureHeader.setAttribute(CLASS_ATTRIBUTE, MEMBER_CLASS);
				aMeasureHeader.setCharacters(measureInfo.getName());
				measureHeaders.add(aMeasureHeader);
			}
		}

		MeasureFormatter measureFormatter = new MeasureFormatter(crossTab);
		int measureHeaderSize = measureHeaders.size();
		for (int i = 0; i < data.length; i++) {
			SourceBean aRow = new SourceBean(ROW_TAG);

			if (crossTab.isMeasureOnRow()) {
				aRow.setAttribute(measureHeaders.get(i % measureHeaderSize));
			}

			String[] values = data[i];
			int pos;
			for (int j = 0; j < values.length; j++) {
				String text = values[j];
				SourceBean aColumn = new SourceBean(COLUMN_TAG);
				CellType cellType = crossTab.getCellType(i, j);
				try {
					// 1. Get specific measure configuration (format, bgcolor, icon visualization,..)
					if (crossTab.isMeasureOnRow()) {
						pos = i % measuresInfo.size();
					} else {
						pos = j % measuresInfo.size();
					}
					JSONObject measureConfig = crossTab.getCrosstabDefinition().getMeasures().get(pos).getConfig();
					String visType = (measureConfig.isNull("visType")) ? "Text" : measureConfig.getString("visType");
					boolean showIcon = false;
					SourceBean iconSB = null;

					if (cellType.getValue().equals("data") && !measureConfig.isNull("scopeFunc")) {
						// check indicator configuration (optional)
						JSONObject indicatorJ = measureConfig.getJSONObject("scopeFunc");
						JSONArray indicatorConditionsJ = indicatorJ.getJSONArray("condition");
						for (int c = 0; c < indicatorConditionsJ.length(); c++) {
							JSONObject condition = indicatorConditionsJ.getJSONObject(c);
							if (iconSB == null && !condition.isNull("value")) {
								// gets icon html
								showIcon = true;
								iconSB = getIconSB(Double.parseDouble(text), condition);
							}
						}
					}

					// 2. define value (number) and its final visualization
					double value = Double.parseDouble(text);
					String actualText = "";
					if (visType.indexOf("Text") >= 0) {
						String patternFormat = null;
						int patternPrecision = 2; // default
						String prefix = null;
						String suffix = null;

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("prefix")) {
							prefix = measureConfig.getJSONObject("style").getString("prefix");
						}

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("suffix")) {
							suffix = measureConfig.getJSONObject("style").getString("suffix");
						}

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("format")) {
							patternFormat = measureConfig.getJSONObject("style").getString("format");
						}

						if (!measureConfig.isNull("style") && !measureConfig.getJSONObject("style").isNull("precision")) {
							patternPrecision = measureConfig.getJSONObject("style").getInt("precision");
						}
						// 3. formatting value...
						actualText = measureFormatter.format(value, patternFormat, patternPrecision, i, j, this.locale);

						String percentOn = crossTab.getCrosstabDefinition().getConfig().optString("percenton");
						if ("row".equals(percentOn) || "column".equals(percentOn)) {
							Double percent = calculatePercent(value, i, j, percentOn, crossTab);
							if (!percent.equals(Double.NaN) && !percent.equals(Double.POSITIVE_INFINITY) && !percent.equals(Double.NEGATIVE_INFINITY)) {
								String percentStr = measureFormatter.formatPercent(percent, this.locale);
								actualText += " (" + percentStr + "%)";
							}
						}

						// 4. prefix and suffix management ...
						if (prefix != null) {
							actualText = prefix + actualText;
						}
						if (suffix != null) {
							actualText += suffix;
						}
					}

					// add icon html if required
					if (showIcon && iconSB != null) {
						actualText += " ";
						aColumn.setAttribute(iconSB);
					}

					String classType = cellType.getValue();
					// 5. style and alignment management
					String dataStyle = getConfiguratedElementStyle(value, cellType, measureConfig, crossTab);
					if (!dataStyle.equals("")) {
						aColumn.setAttribute(STYLE_ATTRIBUTE, dataStyle);
						classType += "noStandardStyle";
					}

					// 6. set value
					// aColumn.setAttribute(CLASS_ATTRIBUTE, cellType.getValue());
					aColumn.setAttribute(CLASS_ATTRIBUTE, classType);
					aColumn.setCharacters(actualText);
				} catch (NumberFormatException e) {
					logger.debug("Text " + text + " is not recognized as a number");
					aColumn.setAttribute(CLASS_ATTRIBUTE, NA_CLASS);
					aColumn.setCharacters(text);
				}
				aRow.setAttribute(aColumn);
			}
			table.setAttribute(aRow);
		}
		return table;
	}

	private String getThresholdColor(double value, JSONObject colorThrJ) throws JSONException {
		String toReturn = "";
		JSONArray thresholdConditions = colorThrJ.getJSONArray("condition");
		JSONObject thresholdConditionValues = (colorThrJ.isNull("conditionValue")) ? null : colorThrJ.getJSONObject("conditionValue");
		JSONObject thresholdColors = (colorThrJ.isNull("color")) ? null : colorThrJ.getJSONObject("color");
		boolean isConditionVerified = false;

		for (int c = 0; c < thresholdConditions.length(); c++) {
			String thrCond = (String) thresholdConditions.get(c);
			if (!thrCond.equalsIgnoreCase("none")) {
				double thrCondValue = thresholdConditionValues.getDouble(String.valueOf(c));
				switch (thrCond) {
				case "<":
					if (value < thrCondValue)
						isConditionVerified = true;
					break;
				case ">":
					if (value > thrCondValue)
						isConditionVerified = true;
					break;
				case "=":
					if (value == thrCondValue)
						isConditionVerified = true;
					break;
				case ">=":
					if (value >= thrCondValue)
						isConditionVerified = true;
					break;
				case "<=":
					if (value <= thrCondValue)
						isConditionVerified = true;
					break;
				case "!=":
					if (value != thrCondValue)
						isConditionVerified = true;
					break;
				default:
					break;
				}
			}
			if (isConditionVerified)
				return thresholdColors.getString(String.valueOf(c));
		}
		return toReturn;
	}

	private String getConfiguratedElementStyle(Double value, CellType cellType, JSONObject config, CrossTab crossTab) throws JSONException {
		JSONObject colorThrJ = null;
		boolean bgColorApplied = false;
		String dataStyle = "";
		String cellTypeValue = (cellType == null) ? "" : cellType.getValue();

		if (cellTypeValue.equalsIgnoreCase("data") && !config.isNull("colorThresholdOptions")) {
			// background management through threshold (optional)
			double dValue = value.doubleValue();
			colorThrJ = config.getJSONObject("colorThresholdOptions");
			String bgThrColor = getThresholdColor(dValue, colorThrJ);
			if (bgThrColor != null && !bgThrColor.equals("")) {
				dataStyle += "background-color:" + bgThrColor + ";";
				bgColorApplied = true;
			}
		}
		// cellType is null for rows and columns header
		// if (cellType == null || (cellType.getValue().equals("data") && !config.isNull("style"))) {
		if (cellTypeValue.equals("") || !config.isNull("style")) {
			JSONObject styleJ = (config.isNull("style")) ? new JSONObject() : config.getJSONObject("style");

			// style management:
			Iterator keys = styleJ.keys();
			while (keys.hasNext()) {
				String keyStyle = (String) keys.next();
				Object valueStyle = styleJ.get(keyStyle);
				if (valueStyle != null) {
					// normalize label properties
					switch (keyStyle) {
					case "textAlign":
						keyStyle = "text-align";
						break;
					case "fontWeight":
						keyStyle = "font-weight";
						break;
					case "fontSize":
						keyStyle = "font-size";
						break;
					case "background":
						if (bgColorApplied || cellTypeValue.equalsIgnoreCase("partialSum") || cellTypeValue.equalsIgnoreCase("totals"))
							continue;
					case "color":
						if (cellTypeValue.equalsIgnoreCase("partialSum") || cellTypeValue.equalsIgnoreCase("totals"))
							continue;
					}
				}
				dataStyle += " " + keyStyle + ":" + String.valueOf(valueStyle) + ";";
			}
		}

		dataStyle += getTotalConfiguration(cellTypeValue, crossTab);

		return dataStyle;
	}

	private String getTotalConfiguration(String cellType, CrossTab crossTab) throws JSONException {
		String toReturn = "";

		if (!cellType.equalsIgnoreCase("partialSum") && !cellType.equalsIgnoreCase("totals"))
			return toReturn;

		JSONObject genericConfig = (!crossTab.getJSONCrossTab().isNull("config")) ? crossTab.getJSONCrossTab().getJSONObject("config") : null;
		JSONObject styleConfig = (genericConfig != null & !genericConfig.isNull("style")) ? genericConfig.getJSONObject("style") : null;

		if (null == styleConfig) {
			if (cellType.equalsIgnoreCase("partialSum"))
				return DEFAULT_BG_SUBTOTALS + DEFAULT_COLOR_TOTALS;
			if (cellType.equalsIgnoreCase("totals"))
				return DEFAULT_BG_TOTALS + DEFAULT_COLOR_TOTALS;
		}

		JSONObject config = new JSONObject();
		if (cellType.equalsIgnoreCase("partialSum")) {
			config = (!styleConfig.isNull("subTotals")) ? styleConfig.getJSONObject("subTotals") : null;
			if (null == config) {
				return DEFAULT_BG_SUBTOTALS + DEFAULT_COLOR_TOTALS;
			}
		}

		if (cellType.equalsIgnoreCase("totals")) {
			config = (!styleConfig.isNull("totals")) ? styleConfig.getJSONObject("totals") : null;
			if (null == config) {
				return DEFAULT_BG_TOTALS + DEFAULT_COLOR_TOTALS;
			}
		}

		if (null != config) {
			Iterator keys = config.keys();
			while (keys.hasNext()) {
				String keyStyle = (String) keys.next();
				Object valueStyle = config.get(keyStyle);
				toReturn += " " + keyStyle + ":" + String.valueOf(valueStyle) + ";";
			}
		}
		return toReturn;
	}

	private SourceBean getIconSB(double value, JSONObject condition) throws JSONException, SourceBeanException {
		SourceBean toReturn = null;
		double condValue = Double.parseDouble(condition.getString("value"));
		String condType = condition.getString("condition");
		boolean isConditionVerified = false;

		switch (condType) {
		case "<":
			if (value < condValue)
				isConditionVerified = true;
			break;
		case ">":
			if (value > condValue)
				isConditionVerified = true;
			break;
		case "=":
			if (value == condValue)
				isConditionVerified = true;
			break;
		case ">=":
			if (value >= condValue)
				isConditionVerified = true;
			break;
		case "<=":
			if (value <= condValue)
				isConditionVerified = true;
			break;
		case "!=":
			if (value != condValue)
				isConditionVerified = true;
			break;
		case "none":
			break;
		default:
			// toReturn = "<md-icon md-font-icon='fa fa-fw'></md-icon>"; //default icon
			toReturn = null;
		}

		if (isConditionVerified) {
			toReturn = new SourceBean(ICON_TAG);
			toReturn.setAttribute(CLASS_ATTRIBUTE, condition.getString("icon"));
			toReturn.setAttribute(STYLE_ATTRIBUTE, "color:" + condition.getString("iconColor"));
		}

		return toReturn;
	}

	private Double calculatePercent(double value, int i, int j, String percentOn, CrossTab crossTab) {
		String[][] entries = crossTab.getDataMatrix();
		int rowSumStartColumn, columnSumStartRow;
		List<MeasureInfo> measures = crossTab.getMeasures();
		int measuresNumber = measures.size();
		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			rowSumStartColumn = entries[0].length - 1;
			columnSumStartRow = entries.length - measuresNumber;
		} else {
			rowSumStartColumn = entries[0].length - measuresNumber;
			columnSumStartRow = entries.length - 1;
		}

		int offset;

		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			offset = i % measuresNumber;
		} else {
			offset = j % measuresNumber;
		}

		if (percentOn.equals("row")) {
			if (!crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100 * value / Double.parseDouble(entries[i][offset + rowSumStartColumn]);
			} else {
				return 100 * value / Double.parseDouble(entries[i][rowSumStartColumn]);
			}
		} else {
			if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
				return 100 * value / Double.parseDouble(entries[offset + columnSumStartRow][j]);
			} else {
				return 100 * value / Double.parseDouble(entries[columnSumStartRow][j]);
			}
		}
	}

	private SourceBean mergeHorizontally(SourceBean left, SourceBean right) throws SourceBeanException {

		SourceBean table = new SourceBean(TABLE_TAG);
		List leftRows = left.getAttributeAsList(ROW_TAG);
		List rightRows = right.getAttributeAsList(ROW_TAG);
		if (leftRows.size() != rightRows.size()) {
			throw new SpagoBIEngineRuntimeException("Cannot merge horizontally 2 tables with a different number of rows");
		}
		for (int i = 0; i < leftRows.size(); i++) {
			SourceBean aLeftRow = (SourceBean) leftRows.get(i);
			SourceBean aRightRow = (SourceBean) rightRows.get(i);
			SourceBean merge = new SourceBean(ROW_TAG);
			List aLeftRowColumns = aLeftRow.getAttributeAsList(COLUMN_TAG);
			for (int j = 0; j < aLeftRowColumns.size(); j++) {
				SourceBean aColumn = (SourceBean) aLeftRowColumns.get(j);
				merge.setAttribute(aColumn);
			}
			List aRightRowColumns = aRightRow.getAttributeAsList(COLUMN_TAG);
			for (int j = 0; j < aRightRowColumns.size(); j++) {
				SourceBean aColumn = (SourceBean) aRightRowColumns.get(j);
				merge.setAttribute(aColumn);
			}
			table.setAttribute(merge);
		}

		return table;
	}

	private SourceBean mergeVertically(SourceBean top, SourceBean bottom) throws SourceBeanException {
		SourceBean table = new SourceBean(TABLE_TAG);
		List topRows = top.getAttributeAsList(ROW_TAG);
		List bottomRows = bottom.getAttributeAsList(ROW_TAG);
		if (topRows == null) {
			topRows = new ArrayList();
		}
		if (bottomRows == null) {
			bottomRows = new ArrayList();
		}
		topRows.addAll(topRows.size(), bottomRows);
		for (int i = 0; i < topRows.size(); i++) {
			SourceBean aRow = (SourceBean) topRows.get(i);
			table.setAttribute(aRow);
		}

		return table;
	}

	private SourceBean addSortArrow(SourceBean aRow, String alias, String style, int direction) throws SourceBeanException {
		SourceBean innerTable = new SourceBean(TABLE_TAG);
		SourceBean innerRow = new SourceBean(ROW_TAG);

		SourceBean div1 = new SourceBean(COLUMN_TAG);
		div1.setCharacters(alias);
		if (style != null && !style.equals(""))
			div1.setAttribute(STYLE_ATTRIBUTE, style);
		else
			div1.setAttribute(CLASS_ATTRIBUTE, "crosstab-header-text");
		SourceBean div2 = new SourceBean(COLUMN_TAG);
		div2.setCharacters(" ");

		if (direction > 0) {
			div2.setAttribute(CLASS_ATTRIBUTE, "sortIcon fa fa-arrow-up");
		} else {
			div2.setAttribute(CLASS_ATTRIBUTE, "sortIcon fa fa-arrow-down");
		}

		innerRow.setAttribute(div2);
		innerRow.setAttribute(div1);
		innerTable.setAttribute(innerRow);

		return innerTable;
	}

	private SourceBean serializeRowsHeaders(CrossTab crossTab) throws SourceBeanException, JSONException {
		List<Row> rows = crossTab.getCrosstabDefinition().getRows();
		SourceBean table = new SourceBean(TABLE_TAG);
		SourceBean aRow = new SourceBean(ROW_TAG);
		boolean addRow = true;
		boolean appliedStyle = false;
		String style = null;
		for (int i = 0; i < rows.size(); i++) {
			Row aRowDef = rows.get(i);
			SourceBean aColumn = new SourceBean(COLUMN_TAG);

			Integer direction = 1;
			if (rowsSortKeysMap != null && rowsSortKeysMap.get(i) != null) {
				direction = rowsSortKeysMap.get(i).getDirection();
			}

			// Get specific rows configuration (format, bgcolor, icon visualization,..)
			JSONObject rowsConfig = rows.get(i).getConfig();
			style = getConfiguratedElementStyle(null, null, rowsConfig, crossTab);
			if (!rowsConfig.isNull("showHeader") && !rowsConfig.getBoolean("showHeader")) {
				// ADD AN EMPTY TD IF THERE IS A HEADER FOR THE MEASURE with the level class
				if (crossTab.getCrosstabDefinition().getMeasures().size() > 1)
					aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
				aRow.setAttribute(aColumn);
				continue; // skips header if not required
			}
			if (!style.equals("")) {
				aColumn.setAttribute(STYLE_ATTRIBUTE, style);
				appliedStyle = true;
			} else
				aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
			// aColumn.setAttribute(ON_CLICK_ATTRIBUTE, "javascript:Sbi.cockpit.widgets.crosstab.HTMLCrossTab.sort('" + i + "','0'," + myGlobalId + ")");
			aColumn.setAttribute(NG_CLICK_ATTRIBUTE, "orderPivotTable('" + i + "','0'," + myGlobalId + ")");
			aColumn.setAttribute(addSortArrow(aRow, aRowDef.getAlias(), style, direction));
			aRow.setAttribute(aColumn);
		}
		if (crossTab.getCrosstabDefinition().isMeasuresOnRows()) {
			SourceBean aColumn = new SourceBean(COLUMN_TAG);
			if (!appliedStyle)
				aColumn.setAttribute(CLASS_ATTRIBUTE, LEVEL_CLASS);
			else
				aColumn.setAttribute(STYLE_ATTRIBUTE, style);

			// aColumn.setCharacters(EngineMessageBundle.getMessage("sbi.crosstab.runtime.headers.measures", this.getLocale()));
			aRow.setAttribute(aColumn);
		}

		// if row is still empty (nothing on rows), add an empty cell
		if (!aRow.containsAttribute(COLUMN_TAG)) {
			SourceBean emptyColumn = new SourceBean(COLUMN_TAG);
			emptyColumn.setAttribute(CLASS_ATTRIBUTE, EMPTY_CLASS);
			aRow.setAttribute(emptyColumn);
		}
		if (addRow)
			table.setAttribute(aRow);
		return table;
	}

	private SourceBean serializeTopLeftCorner(CrossTab crossTab) throws SourceBeanException, JSONException {
		int columnHeadersVerticalDepth = crossTab.getColumnsRoot().getDistanceFromLeaves();
		int rowHeadersHorizontalDepth = crossTab.getRowsRoot().getDistanceFromLeaves();
		boolean hideRow = false;
		// check the columns header visibility on columns
		List<Column> columns = crossTab.getCrosstabDefinition().getColumns();
		for (int c = 0; c < columns.size(); c++) {
			JSONObject colConf = columns.get(c).getConfig();
			if (!colConf.isNull("showHeader") && !colConf.getBoolean("showHeader")) {
				columnHeadersVerticalDepth--;
				hideRow = true;
			}
		}
		// for measures hide header only if there is only one
		if (!crossTab.isMeasureOnRow()) {
			boolean hideHeaderMeasure = false;
			List<Measure> measures = crossTab.getCrosstabDefinition().getMeasures();
			if (measures.size() == 1) {
				JSONObject colMeasure = measures.get(0).getConfig();
				if (!colMeasure.isNull("showHeader") && !colMeasure.getBoolean("showHeader")) {
					hideHeaderMeasure = true;
				} else {
					hideHeaderMeasure = false;
				}
			}
			if (hideHeaderMeasure)
				columnHeadersVerticalDepth--;
		}

		int numberOfEmptyRows = columnHeadersVerticalDepth - 1; // one row is
																// dedicated to
																// rows' headers
		SourceBean table = new SourceBean(TABLE_TAG);
		for (int i = 0; i < numberOfEmptyRows; i++) {
			SourceBean emptyRow = new SourceBean(ROW_TAG);
			SourceBean emptyColumn = new SourceBean(COLUMN_TAG);
			emptyColumn.setAttribute(CLASS_ATTRIBUTE, EMPTY_CLASS);
			emptyRow.setAttribute(emptyColumn);
			// if headers are hidden add a td field ONLY when there are more than one measure
			if (crossTab.getCrosstabDefinition().getMeasures().size() > 1) {
				emptyRow.setAttribute(emptyColumn);
			}
			table.setAttribute(emptyRow);
		}
		return table;
	}

}
