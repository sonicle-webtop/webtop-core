/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.io.input;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.ss.util.CellReference;

/**
 *
 * @author malbinola
 */
public abstract class XlsRecordsProcessor extends XlsBaseProcessor implements HSSFListener {
	protected final int headersRow;
	protected final int firstDataRow;
	protected final int lastDataRow;
	protected final String sheetName;
	protected ColumnMapper mapper;
	protected boolean sheetFound = false;
	private SSTRecord sstRecord = null;
	protected int row = -1;
	protected int col = -1;
	protected String cellValue = null;
	protected boolean isNewRow = false;
	protected boolean isDummyEndRow = false;
	protected boolean isInRange = false;
	private boolean findNextStringRecord = false;
	protected int nextRow = -1;
	protected int nextCol = -1;
	public LinkedHashMap<String, String> columnsMapping;
	public HashMap<String, Integer> columnIndexes;
	
	@Override
	protected abstract HSSFRequest createRequest();
	
	public XlsRecordsProcessor(InputStream is, int headersRow, int firstDataRow, int lastDataRow, String sheetName, ColumnMapper mapper) {
		super(is);
		// Converts to 0-based indexes
		this.headersRow = headersRow-1;
		this.firstDataRow = firstDataRow-1;
		this.lastDataRow = (lastDataRow >= 0) ? lastDataRow-1 : lastDataRow;
		this.sheetName = sheetName;
		this.mapper = mapper;
	}
	
	@Override
	public void processRecord(Record record) {
		int thisRow = -1;
		int thisCol = -1;
		cellValue = null;
		
		switch(record.getSid()) {
			case BoundSheetRecord.sid:
				BoundSheetRecord bsr = (BoundSheetRecord) record;
				if(!sheetFound) {
					if(StringUtils.equals(bsr.getSheetname(), sheetName)) {
						sheetFound = true;
						columnsMapping = new LinkedHashMap<>();
						columnIndexes = new HashMap<>();
					}
				} else {
					close();
				}
				break;
				
			case SSTRecord.sid:
				sstRecord = (SSTRecord)record;
				break;
				
			case BlankRecord.sid:
				BlankRecord br = (BlankRecord)record;
				thisRow = br.getRow();
				thisCol = br.getColumn();
				cellValue = "";
				break;
				
			case BoolErrRecord.sid:
				BoolErrRecord ber = (BoolErrRecord)record;
				thisRow = ber.getRow();
				thisCol = ber.getColumn();
				cellValue = "";
				break;
				
			case FormulaRecord.sid:
				FormulaRecord fr = (FormulaRecord)record;
				thisRow = fr.getRow();
				thisCol = fr.getColumn();
				if(Double.isNaN(fr.getValue())) {
					// Formula result is a string that is stored in the next record!
					findNextStringRecord = true;
					nextRow = fr.getRow();
					nextCol = fr.getColumn();
					cellValue = null;
				} else {
					cellValue = formatTrackingListener.formatNumberDateCell(fr);
				}
				break;
				
			case StringRecord.sid:
				if(findNextStringRecord) {
					// String for formula 
					StringRecord sr = (StringRecord)record;
					cellValue = sr.getString();
					thisRow = nextRow;
					thisCol = nextCol;
					// Resets markers...
					findNextStringRecord = false;
					nextRow = -1;
					nextCol = -1;
				}
				break;
				
			case LabelRecord.sid:
				LabelRecord lr = (LabelRecord)record;
				thisRow = lr.getRow();
				thisCol = lr.getColumn();
				cellValue = lr.getValue();
				break;
				
			case LabelSSTRecord.sid:
				LabelSSTRecord lsstr = (LabelSSTRecord)record;
				thisRow = lsstr.getRow();
				thisCol = lsstr.getColumn();
				if (sstRecord == null) {
					cellValue = "#ERROR(undefined string)";
				} else {
					cellValue = sstRecord.getString(lsstr.getSSTIndex()).toString();
				}
				break;
				
			case NoteRecord.sid:
				NoteRecord nr = (NoteRecord)record;
				thisRow = nr.getRow();
				thisCol = nr.getColumn();
				// TODO: Find object to match nrec.getShapeId() 
				cellValue = "#ERROR(TODO)";
				break;
				
			case NumberRecord.sid:
				NumberRecord rn = (NumberRecord)record;
				thisRow = rn.getRow();
				thisCol = rn.getColumn();
				cellValue = formatTrackingListener.formatNumberDateCell(rn);
				break;
				
			case RKRecord.sid:
				RKRecord rkr = (RKRecord)record;
				thisRow = rkr.getRow();
				thisCol = rkr.getColumn();
				cellValue = "#ERROR(TODO)";
				break;
				
			default:
				cellValue = null;
		}
		
		// Handle new row
		if((thisRow != -1) && (thisRow != row)) {
			row = -1;
			isNewRow = true;
		} else {
			isNewRow = false;
		}
		if(thisRow > -1) row = thisRow;
		if(thisCol > -1) col = thisCol;
		isInRange = ((row >= firstDataRow) && ((lastDataRow == -1) || (row <= lastDataRow)));
		
		// Handle end of row
		if(record instanceof LastCellOfRowDummyRecord) {
			isDummyEndRow = true;
			col = -1; // We're nearly onto a new row
		} else {
			isDummyEndRow = false;
		}
		
		if(!isDummyEndRow && (row == headersRow)) {
			String cellReference = CellReference.convertNumToColString(col);
			String name = (headersRow == firstDataRow) ? cellReference : StringUtils.defaultIfBlank(cellValue, cellReference);
			if (mapper != null) columnsMapping.put(mapper.mapColumnName(name), name);
			columnIndexes.put(name, col);
		}
	}
	
	public static interface ColumnMapper {
		public String mapColumnName(String columnName);
	}
}
