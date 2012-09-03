package it.unibz.krdb.obda.protege4.gui.component;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.protege4.gui.IconLoader;
import it.unibz.krdb.obda.protege4.gui.action.EditableCellFocusAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class PropertyEditorList extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;

	private OBDAModel obdaModel;
	private PrefixManager prefixManager;
	
	private static final Color SELECTION_BACKGROUND = UIManager.getDefaults().getColor("List.selectionBackground");
	private static final Color NORMAL_BACKGROUND = new Color(240, 245, 240);
	
	public PropertyEditorList(OBDAModel obdaModel) {
		this.obdaModel = obdaModel;
		prefixManager = obdaModel.getPrefixManager();
		initComponents();
	}

	public List<MapItem> getPredicateObjectMapsList() {
		DefaultTableModel propertyListModel = (DefaultTableModel) lstProperties.getModel();
		int column = 0; // it's a single column table
		int totalRow = propertyListModel.getRowCount();
		
		ArrayList<MapItem> predicateObjectMapList = new ArrayList<MapItem>();
		for (int row = 0; row < totalRow; row++) {
			predicateObjectMapList.add((MapItem) propertyListModel.getValueAt(row, column));
		}
		return predicateObjectMapList;
	}

	public boolean isEditing() {
		return lstProperties.isEditing();
	}

	public boolean stopCellEditing() {
		return lstProperties.getCellEditor().stopCellEditing();
	}
	
	public void clear() {
		cboPropertyAutoSuggest.setSelectedIndex(-1);
		((DefaultTableModel) lstProperties.getModel()).setRowCount(0);
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAddProperty = new javax.swing.JPanel();
        cmdAdd = new javax.swing.JButton();
        scrPropertyList = new javax.swing.JScrollPane();
        lstProperties = new javax.swing.JTable();

        setAlignmentX(5.0F);
        setAlignmentY(5.0F);
        setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        setMinimumSize(new java.awt.Dimension(280, 500));
        setPreferredSize(new java.awt.Dimension(280, 500));
        setLayout(new java.awt.BorderLayout());

        pnlAddProperty.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 3, 0));
        pnlAddProperty.setLayout(new java.awt.BorderLayout(3, 0));
        Vector<Object> v = new Vector<Object>();
        for (Predicate dp : obdaModel.getDeclaredDataProperties()) {
            v.addElement(new PredicateItem(dp, prefixManager));
        }
        for (Predicate op : obdaModel.getDeclaredObjectProperties()) {
            v.addElement(new PredicateItem(op, prefixManager));
        }
        cboPropertyAutoSuggest = new AutoSuggestComboBox(v);
        cboPropertyAutoSuggest.setMinimumSize(new java.awt.Dimension(195, 23));
        cboPropertyAutoSuggest.setPreferredSize(new java.awt.Dimension(195, 23));
        JTextField txtComboBoxEditor = (JTextField) cboPropertyAutoSuggest.getEditor().getEditorComponent();
        txtComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cboPropertyAutoSuggestKeyPressed(evt);
            }
        });
        pnlAddProperty.add(cboPropertyAutoSuggest);

        cmdAdd.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdAdd.setContentAreaFilled(false);
        cmdAdd.setMaximumSize(new java.awt.Dimension(23, 23));
        cmdAdd.setMinimumSize(new java.awt.Dimension(23, 23));
        cmdAdd.setPreferredSize(new java.awt.Dimension(23, 23));
        cmdAdd.setIcon(IconLoader.getImageIcon("images/plus.png"));
        cmdAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddActionPerformed(evt);
            }
        });
        pnlAddProperty.add(cmdAdd, java.awt.BorderLayout.EAST);

        add(pnlAddProperty, java.awt.BorderLayout.PAGE_START);

        lstProperties.setModel(new DefaultTableModel(0, 1));
        lstProperties.setCellSelectionEnabled(true);
        lstProperties.setRowHeight(57);
        lstProperties.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstProperties.setTableHeader(null);
        lstProperties.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstPropertiesKeyPressed(evt);
            }
        });
        new EditableCellFocusAction(lstProperties, KeyStroke.getKeyStroke("ENTER"));
        new EditableCellFocusAction(lstProperties, KeyStroke.getKeyStroke("F2"));
        scrPropertyList.setViewportView(lstProperties);

        add(scrPropertyList, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

	private void lstPropertiesKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_lstPropertiesKeyPressed
		int code = evt.getKeyCode();
		if (code == KeyEvent.VK_DELETE) {
			TableCellEditor editor = lstProperties.getCellEditor();
			if (editor != null) {
				editor.stopCellEditing();
			}
			((DefaultTableModel) lstProperties.getModel()).removeRow(lstProperties.getSelectedRow());
		}
	}// GEN-LAST:event_lstPropertiesKeyPressed

	private void cboPropertyAutoSuggestKeyPressed(KeyEvent evt) {
		int code = evt.getKeyCode();
		if (code == KeyEvent.VK_ESCAPE) {
			cboPropertyAutoSuggest.setSelectedIndex(-1);
		} else if (code == KeyEvent.VK_ENTER) {
			Object obj = cboPropertyAutoSuggest.getSelectedItem();
			if (obj instanceof PredicateItem) {
				PredicateItem selectedItem = (PredicateItem) obj;
				addRow(selectedItem);
			}
		}
	}

	private void cmdAddActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdAddActionPerformed
		Object obj = cboPropertyAutoSuggest.getSelectedItem();
		if (obj instanceof PredicateItem) {
			PredicateItem selectedItem = (PredicateItem) obj;
			addRow(selectedItem);
		}
	}// GEN-LAST:event_cmdAddActionPerformed	
	
	private void addRow(PredicateItem selectedItem) {
		MapItem predicateObjectMap = new MapItem(selectedItem);
		if (selectedItem.isObjectPropertyPredicate()) {
			String defaultNamespace = prefixManager.getDefaultPrefix();
			predicateObjectMap.setTargetMapping(prefixManager.getShortForm(defaultNamespace, true));
		}
		
		// Insert the selected item from the combo box to the table as a new table cell
		MapItem[] row = new MapItem[1];
		row[0] = predicateObjectMap;
		((DefaultTableModel) lstProperties.getModel()).addRow(row);
		
		// Define for each added table cell a custom renderer and editor
		TableColumn col = lstProperties.getColumnModel().getColumn(0);
		
		// Add custom cell renderer
		col.setCellRenderer(new PropertyItemRenderer());
		
		// Add custom cell editor
		PropertyItemEditor editor = new PropertyItemEditor();
		editor.addCellEditorListener(lstProperties);
		col.setCellEditor(editor);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdAdd;
    private javax.swing.JTable lstProperties;
    private javax.swing.JPanel pnlAddProperty;
    private AutoSuggestComboBox cboPropertyAutoSuggest;
    private javax.swing.JScrollPane scrPropertyList;
    // End of variables declaration//GEN-END:variables

	class PropertyItemRenderer extends JPanel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
		
		private JPanel pnlPropertyName;
		private JPanel pnlPropertyUriTemplate;
		private JLabel lblPropertyName;
		private DataTypeComboBox cboDataTypes;
		private JLabel lblMapIcon;
		private JTextField txtPropertyTargetMap;

		public PropertyItemRenderer() {
			initComponents();
		}

		private void initComponents() {
			setBackground(NORMAL_BACKGROUND);
			setLayout(new BorderLayout(0, 2));
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(192, 192, 192), 1), 
					BorderFactory.createEmptyBorder(4, 4, 4, 4))
			);
			setFocusable(false);
			
			pnlPropertyName = new JPanel();
			pnlPropertyUriTemplate = new JPanel();
			lblPropertyName = new JLabel();
			cboDataTypes = new DataTypeComboBox();
			lblMapIcon = new JLabel();
			txtPropertyTargetMap = new JTextField();
			
			lblPropertyName.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
			
			cboDataTypes.setBackground(Color.WHITE);
			cboDataTypes.setSelectedIndex(-1);
			
			pnlPropertyName.setLayout(new BorderLayout(5, 0));
			pnlPropertyName.setOpaque(false);
			pnlPropertyName.setFocusable(false);
			pnlPropertyName.add(lblPropertyName, BorderLayout.WEST);
			pnlPropertyName.add(cboDataTypes, BorderLayout.EAST);
			
			lblMapIcon.setIcon(IconLoader.getImageIcon("images/link.png"));
			
			pnlPropertyUriTemplate.setLayout(new BorderLayout(5, 0));
			pnlPropertyUriTemplate.setOpaque(false);
			pnlPropertyUriTemplate.setFocusable(false);
			pnlPropertyUriTemplate.add(lblMapIcon, BorderLayout.WEST);
			pnlPropertyUriTemplate.add(txtPropertyTargetMap, BorderLayout.CENTER);

			add(pnlPropertyName, BorderLayout.NORTH);
			add(pnlPropertyUriTemplate, BorderLayout.SOUTH);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			if (isSelected) {
				setBackground(SELECTION_BACKGROUND);
			} else {
				setBackground(NORMAL_BACKGROUND);
			}
			if (value instanceof MapItem) {
				MapItem entry = (MapItem) value;
				lblPropertyName.setText(entry.toString());
				if (entry.isObjectMap()) {
					cboDataTypes.setVisible(true);
					cboDataTypes.setSelectedItem(entry.getDataType());
					lblPropertyName.setIcon(IconLoader.getImageIcon("images/data_property.png"));
					txtPropertyTargetMap.setText(entry.getTargetMapping());
				} else if (entry.isRefObjectMap()) {
					cboDataTypes.setVisible(false);
					lblPropertyName.setIcon(IconLoader.getImageIcon("images/object_property.png"));
					txtPropertyTargetMap.setText(prefixManager.getShortForm(entry.getTargetMapping(), true));
				}
			}
			return this;
		}
	}
	
	public class PropertyItemEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;
		
		private JPanel pnlPropertyMapCell;
		private JPanel pnlPropertyName;
		private JPanel pnlPropertyUriTemplate;
		private JLabel lblPropertyName;
		private DataTypeComboBox cboDataTypes;
		private JLabel lblMapIcon;
		private JTextField txtPropertyTargetMap;
		
		private MapItem editedItem;

		public PropertyItemEditor() {
			initComponents();
		}
		
		private void setCaretToTextField() {
			txtPropertyTargetMap.requestFocusInWindow();
			txtPropertyTargetMap.setCaretPosition(txtPropertyTargetMap.getText().length());
		}

		private void initComponents() {
			pnlPropertyMapCell = new JPanel() {
				public void addNotify() {
					super.addNotify();
					setCaretToTextField();
				}
			};
			pnlPropertyMapCell.setRequestFocusEnabled(true);
			pnlPropertyName = new JPanel();
			pnlPropertyUriTemplate = new JPanel();
			lblPropertyName = new JLabel();
			cboDataTypes = new DataTypeComboBox();
			lblMapIcon = new JLabel();
			txtPropertyTargetMap = new JTextField();
			
			pnlPropertyMapCell.setLayout(new BorderLayout(0, 2));
			pnlPropertyMapCell.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(192, 192, 192), 1), 
					BorderFactory.createEmptyBorder(4, 4, 4, 4))
			);
			pnlPropertyMapCell.setRequestFocusEnabled(true);
			
			lblPropertyName.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 12));
			
			cboDataTypes.setBackground(Color.WHITE);
			cboDataTypes.setSelectedIndex(-1);
			
			pnlPropertyName.setLayout(new BorderLayout(5, 0));
			pnlPropertyName.setOpaque(false);
			pnlPropertyName.add(lblPropertyName, BorderLayout.WEST);
			pnlPropertyName.add(cboDataTypes, BorderLayout.EAST);
			
			lblMapIcon.setIcon(IconLoader.getImageIcon("images/link.png"));
			
			pnlPropertyUriTemplate.setLayout(new BorderLayout(5, 0));
			pnlPropertyUriTemplate.setOpaque(false);
			pnlPropertyUriTemplate.add(lblMapIcon, BorderLayout.WEST);
			pnlPropertyUriTemplate.add(txtPropertyTargetMap, BorderLayout.CENTER);

			pnlPropertyMapCell.add(pnlPropertyName, BorderLayout.NORTH);
			pnlPropertyMapCell.add(pnlPropertyUriTemplate, BorderLayout.SOUTH);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (!isSelected) {
				pnlPropertyMapCell.setBackground(SELECTION_BACKGROUND);
			}
			if (value instanceof MapItem) {
				MapItem entry = (MapItem) value;
				lblPropertyName.setText(entry.toString());
				editedItem = entry;
				if (entry.isObjectMap()) {
					cboDataTypes.setVisible(true);
					cboDataTypes.setSelectedItem(entry.getDataType());
					lblPropertyName.setIcon(IconLoader.getImageIcon("images/data_property.png"));
					txtPropertyTargetMap.setText(entry.getTargetMapping());
				} else if (entry.isRefObjectMap()) {
					cboDataTypes.setVisible(false);
					lblPropertyName.setIcon(IconLoader.getImageIcon("images/object_property.png"));
					txtPropertyTargetMap.setText(prefixManager.getShortForm(entry.getTargetMapping(), true));
				}
			}
			return pnlPropertyMapCell;
		}

		@Override
		public Object getCellEditorValue() {
			if (editedItem != null) {
				if (editedItem.isObjectMap()) {
					editedItem.setDataType((Predicate) cboDataTypes.getSelectedItem());
					editedItem.setTargetMapping(txtPropertyTargetMap.getText());
				} else if (editedItem.isRefObjectMap()) {
					editedItem.setTargetMapping(prefixManager.getExpandForm(txtPropertyTargetMap.getText(), true));
				}
			}
			return editedItem;
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent) anEvent;
				if (mouseEvent.getClickCount() == 1) {
					return true;
				}
			} else if (anEvent instanceof ActionEvent) {
				return true;
			}
			return false;
		}
	}
}
