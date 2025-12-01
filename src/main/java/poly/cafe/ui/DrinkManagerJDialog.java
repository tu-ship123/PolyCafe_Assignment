/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import poly.cafe.dao.CategoryDAO;
import poly.cafe.dao.DrinkDAO;
import poly.cafe.entity.Category;
import poly.cafe.entity.Drink;
import poly.cafe.util.MsgBox;
import poly.cafe.util.XImage;

/**
 *
 * @author acer
 */
public class DrinkManagerJDialog extends javax.swing.JDialog {
    DrinkDAO dao = new DrinkDAO();
    int row = -1; // Hàng được chọn trên bảng

    void init() {
        setIconImage(XImage.getAppIcon());
        setLocationRelativeTo(null);
        setTitle("DANH MỤC ĐỒ UỐNG");
        fillCategories();
        clearForm();
    }
    /**
     * Creates new form DrinkManagerJDialog
     */
    public DrinkManagerJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        init();
    }
    
    public void fillCategories() {
        DefaultComboBoxModel<String> cboModel = new DefaultComboBoxModel<>();
        CategoryDAO dao = new CategoryDAO();

        try {
            List<Category> list = dao.selectAll();
            for (Category category : list) {
                cboModel.addElement(category.getName()); // chỉ thêm tên
            }
            ListBoxCategory.setModel(cboModel); // Gán lại ListBoxCategory
            cboCategory.setModel(cboModel); // gán lại cboCategory
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi truy vấn dữ liệu!");
            e.printStackTrace();
        }
    }

    void fillTableByCategory() {
        DefaultTableModel model = (DefaultTableModel) tblDrink.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        // Truy vấn đến name Category, từ name => id của category:
        CategoryDAO cdao = new CategoryDAO();
        String categoryName = ListBoxCategory.getSelectedValue(); // lấy tên khi chọn dòng trên listBox
        String id = cdao.getCategoryIdByName(categoryName);

        try {
            List<Drink> list = dao.selectAllCategoryID(id);
            for (Drink d : list) {
                Object[] row = {
                    d.getId(),
                    d.getName(),
                    String.format("$%.1f", d.getUnitPrice()),
                    String.format("%.0f%%", d.getDiscount() * 100),
                    d.isAvailable() ? "Sẵn có" : "Hết hàng"
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi truy vấn dữ liệu!");
            e.printStackTrace();
        }
    }
    
    private void selectAll() {
    for (int i = 0; i < tblDrink.getRowCount(); i++) {
        tblDrink.setValueAt(true, i, 5);
    }
}

private void noSelectAll() {
    for (int i = 0; i < tblDrink.getRowCount(); i++) {
        tblDrink.setValueAt(false, i, 5);
    }
}
    public void deleteCategory() {
    if (MsgBox.confirm(this, "Bạn đang thực hiện chức năng xoá")) {
        boolean kq = false;
        for (int i = tblDrink.getRowCount() - 1; i >= 0; i--) {
            String id = tblDrink.getValueAt(i, 0).toString();
            Object value = tblDrink.getValueAt(i, 5);
            if (value != null && (Boolean) value) {
                try {
                    dao.delete(id);
                    kq = true;
                } catch (Exception e) {
                    MsgBox.alert(this, "Lỗi: " + e.getMessage());
                    continue;
                }
            }
        }
    if (kq) {
    MsgBox.alert(this, "Xoá thành công");
    } else {
        MsgBox.alert(this, "Bạn chọn lại dòng để xoá");
    }
    this.fillCategories();
    }
}
    public boolean ValidateForm() {
    String id = txtIDDrink.getText().trim();
    String name = txtIDNameDrink.getText().trim();
    String priceText = txtPriceDrink.getText().trim();
    String type = (String) cboCategory.getSelectedItem();

    // Mã đồ uống
    if (id.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập mã đồ uống!");
        return false;
    }

    // Tên đồ uống
    if (name.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập tên đồ uống!");
        return false;
    }

    // Đơn giá
    if (priceText.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập đơn giá!");
        return false;
    }
    try {
        double price = Double.parseDouble(priceText);
        if (price < 0) {
            MsgBox.alert(this, "Đơn giá phải lớn hơn hoặc bằng 0!");
            return false;
        }
    } catch (NumberFormatException e) {
        MsgBox.alert(this, "Đơn giá phải là một số!");
        return false;
    }

    // Loại đồ uống
    if (type == null || type.trim().isEmpty()) {
        MsgBox.alert(this, "Vui lòng chọn loại đồ uống!");
        return false;
    }

    // Trạng thái
    if (!rdoAvailable.isSelected() && !rdoOutOfStock.isSelected()) {
        MsgBox.alert(this, "Vui lòng chọn trạng thái!");
        return false;
    }

    return true;
}
    public Drink getDrinkForm() {
    Drink d = new Drink();
    d.setId(txtIDDrink.getText().trim());
    d.setName(txtIDNameDrink.getText().trim());
    d.setUnitPrice(Double.parseDouble(txtPriceDrink.getText().trim()));
    d.setDiscount((double) jSliderDiscount.getValue() / 100); // nếu bạn dùng slider
    d.setAvailable(rdoAvailable.isSelected());
    
    // lấy category ID từ tên
    String categoryName = (String) cboCategory.getSelectedItem();
    String categoryId = new CategoryDAO().getCategoryIdByName(categoryName);
    d.setCategoryId(categoryId);

    return d;
}
    void setForm(Drink d) {
    txtIDDrink.setText(d.getId());
    txtIDNameDrink.setText(d.getName());
    txtPriceDrink.setText(String.valueOf(d.getUnitPrice()));
    
    int discount = (int) (d.getDiscount() * 100);
    jSliderDiscount.setValue(discount); // nếu bạn dùng slider

    rdoAvailable.setSelected(d.isAvailable());
    rdoOutOfStock.setSelected(!d.isAvailable());

    // chọn lại combobox theo tên category
    String categoryId = d.getCategoryId();
    String categoryName = new CategoryDAO().getCategoryIdByName(categoryId);
    cboCategory.setSelectedItem(categoryName);
}

void clearForm() {
    txtIDDrink.setText("");
    txtIDNameDrink.setText("");
    txtPriceDrink.setText("");
    jSliderDiscount.setValue(0); // Slider về 0%
    cboCategory.setSelectedIndex(0); // Về mục đầu tiên (nếu có)
    buttonGroup1.clearSelection(); // Bỏ chọn radio button "Sẵn có" và "Hết hàng"
    
    row = -1;
    this.setEditable(false);
}
    public Category getForm() {
    Category cd = new Category();
    cd.setId(txtIDDrink.getText().trim());
    cd.setName(txtIDNameDrink.getText().trim());
    return cd;
}

private void clearTableSelectionAndEditor() {
    TableCellEditor editor = tblDrink.getCellEditor();
    if (editor != null) {
        editor.stopCellEditing();
    }
    tblDrink.clearSelection();
    this.requestFocusInWindow();
}
void edit() {
    row = tblDrink.getSelectedRow();
    try {
        String id = tblDrink.getValueAt(row, 0).toString();
        Drink d = dao.selectByID(id); // dùng Drink thay vì Category
        if (d != null) {
            this.setForm(d); // truyền đối tượng Drink vào setForm
            this.setEditable(true); 
            Tabs.setSelectedIndex(1);
            clearTableSelectionAndEditor();
        }
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi truy vấn dữ liệu");
    }
}
    void insert() {
    if (!ValidateForm()) {
        return;
    }

    Drink d = getDrinkForm();

    if (dao.selectByID(d.getId()) != null) {
        MsgBox.alert(this, "ID đã tồn tại");
        txtIDDrink.requestFocus();
        return;
    }

    try {
        dao.insert(d);
        this.fillCategories();
        this.clearForm();
        MsgBox.alert(this, "Thêm dữ liệu thành công");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi: " + e.getMessage());
    }
}
    void update() {
    if (!ValidateForm()) {
        return;
    }

    Drink d = getDrinkForm(); // lấy form từ hàm đúng kiểu

    if (dao.selectByID(d.getId()) == null) {
        MsgBox.alert(this, "Không tồn tại ID để cập nhật");
        return;
    }

    try {
        dao.update(d);
        this.fillCategories();
        this.clearForm();
        MsgBox.alert(this, "Cập nhật dữ liệu thành công");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi: " + e.getMessage());
    }
}
    void delete() {
    String id = txtIDDrink.getText().trim();
    if (id.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập mã Category để xoá");
        return;
    }

    if (dao.selectByID(id) == null) {
        MsgBox.alert(this, "Không tồn tại mã để xoá");
        return;
    }

    try {
        dao.delete(id);
        this.fillCategories();
        this.clearForm();
        MsgBox.alert(this, "Xoá dữ liệu thành công");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi: " + e.getMessage());
    }
}
    
    void setEditable(boolean editable) {
    txtIDDrink.setEnabled(!editable);     // Mã chỉ nhập khi thêm mới
    btnAdd.setEnabled(!editable);         // Tạo mới chỉ khi không trong chế độ sửa
    btnUpdate.setEnabled(editable);          // Chỉ bật khi đang sửa
    btnDelete.setEnabled(editable);          // Chỉ bật khi đang sửa
    
    int rowCount = tblDrink.getRowCount();
    btnMoveFirst.setEnabled(editable && rowCount > 0);
    btnMovePrevious.setEnabled(editable && rowCount > 0);
    btnMoveNext.setEnabled(editable && rowCount > 0);
    btnMoveLast.setEnabled(editable && rowCount > 0);
}
    void moveFirst() {
    moveTo(0);
}

void movePrevious() {
    int index = tblDrink.getSelectedRow() - 1;
    moveTo(index);
}

void moveNext() {
    int index = tblDrink.getSelectedRow() + 1;
    moveTo(index);
}

void moveLast() {
    int lastIndex = tblDrink.getRowCount() - 1;
    moveTo(lastIndex);
}

void moveTo(int index) {
    int rowCount = tblDrink.getRowCount();

    if (rowCount == 0) return;

    if (index < 0) {
        moveLast();
    } else if (index >= rowCount) {
        moveFirst();
    } else {
        tblDrink.clearSelection();
        tblDrink.setRowSelectionInterval(index, index);
        edit();
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        Tabs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDrink = new javax.swing.JTable();
        btnSelectAll = new javax.swing.JButton();
        btnNoSelect = new javax.swing.JButton();
        btnDeleteSelect = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ListBoxCategory = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtIDDrink = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnMoveFirst = new javax.swing.JButton();
        btnMovePrevious = new javax.swing.JButton();
        btnMoveNext = new javax.swing.JButton();
        btnMoveLast = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtIDNameDrink = new javax.swing.JTextField();
        txtPriceDrink = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jSliderDiscount = new javax.swing.JSlider();
        jLabel7 = new javax.swing.JLabel();
        cboCategory = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        rdoAvailable = new javax.swing.JRadioButton();
        rdoOutOfStock = new javax.swing.JRadioButton();
        lblSlider = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        Tabs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabsMouseClicked(evt);
            }
        });

        jScrollPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jScrollPane1MousePressed(evt);
            }
        });

        tblDrink.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã đồ uống", "Tên đồ uống", "Đơn giá", "Giảm giá", "Trạng thái", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblDrink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblDrinkMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblDrink);

        btnSelectAll.setText("Chọn tất cả");
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        btnNoSelect.setText("Bỏ chọn tất cả");
        btnNoSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNoSelectActionPerformed(evt);
            }
        });

        btnDeleteSelect.setText("Xóa các mục chọn");
        btnDeleteSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSelectActionPerformed(evt);
            }
        });

        ListBoxCategory.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Cafe", "Nước ngọt", "Nước trái cây", "Sinh tố", "Trà sữa" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        ListBoxCategory.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                ListBoxCategoryValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(ListBoxCategory);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Loại đồ uống");
        jLabel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelectAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteSelect)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectAll)
                    .addComponent(btnNoSelect)
                    .addComponent(btnDeleteSelect))
                .addContainerGap())
        );

        Tabs.addTab("DANH SÁCH", jPanel1);

        jLabel1.setText("Mã đồ uống");

        jLabel2.setText("Đơn giá");

        btnAdd.setText("Tạo mới");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdate.setText("Cập nhật");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Xóa");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnClear.setText("Nhập mới");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnMoveFirst.setText("|<");
        btnMoveFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveFirstActionPerformed(evt);
            }
        });

        btnMovePrevious.setText("<<");
        btnMovePrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMovePreviousActionPerformed(evt);
            }
        });

        btnMoveNext.setText(">>");
        btnMoveNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveNextActionPerformed(evt);
            }
        });

        btnMoveLast.setText(">|");
        btnMoveLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveLastActionPerformed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/poly/cafe/icons/trump.png"))); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 0)));

        jLabel5.setText("Tên đồ uống");

        jLabel6.setText("Giảm giá");

        jSliderDiscount.setValue(0);
        jSliderDiscount.setFocusable(false);
        jSliderDiscount.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderDiscountStateChanged(evt);
            }
        });

        jLabel7.setText("Loại");

        cboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cafe", "Nước ngọt", "Nước trái cây", "Sinh tố", "Trà sữa" }));

        jLabel8.setText("Trạng thái");

        buttonGroup1.add(rdoAvailable);
        rdoAvailable.setText("Sẵn có");

        buttonGroup1.add(rdoOutOfStock);
        rdoOutOfStock.setText("Hết hàng");

        lblSlider.setText("0%");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClear)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMoveFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMovePrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveLast, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(247, 247, 247)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtPriceDrink, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                        .addComponent(txtIDDrink, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(50, 50, 50)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtIDNameDrink)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(rdoAvailable, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rdoOutOfStock))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jSliderDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblSlider)))))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtIDDrink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPriceDrink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(rdoAvailable)
                                        .addComponent(rdoOutOfStock))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtIDNameDrink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel6)
                                .addGap(12, 12, 12)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jSliderDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSlider)))))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMoveLast)
                    .addComponent(btnMoveNext)
                    .addComponent(btnMovePrevious)
                    .addComponent(btnMoveFirst)
                    .addComponent(btnClear)
                    .addComponent(btnDelete)
                    .addComponent(btnUpdate)
                    .addComponent(btnAdd))
                .addContainerGap())
        );

        Tabs.addTab("BIỂU MẪU", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Tabs)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jScrollPane1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MousePressed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jScrollPane1MousePressed

    private void jSliderDiscountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderDiscountStateChanged
        // TODO add your handling code here:
        int value = jSliderDiscount.getValue();
        lblSlider.setText(value + "%");
    }//GEN-LAST:event_jSliderDiscountStateChanged

    private void ListBoxCategoryValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_ListBoxCategoryValueChanged
        // TODO add your handling code here:
        fillTableByCategory();
    }//GEN-LAST:event_ListBoxCategoryValueChanged

    private void tblDrinkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDrinkMousePressed
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            edit();
        }
    }//GEN-LAST:event_tblDrinkMousePressed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnNoSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoSelectActionPerformed
        // TODO add your handling code here:
        noSelectAll();
    }//GEN-LAST:event_btnNoSelectActionPerformed

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed
        // TODO add your handling code here:
        selectAll();
    }//GEN-LAST:event_btnSelectAllActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        insert();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void TabsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabsMouseClicked
        // TODO add your handling code here:
        clearTableSelectionAndEditor();
    }//GEN-LAST:event_TabsMouseClicked

    private void btnDeleteSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectActionPerformed
        // TODO add your handling code here:
        deleteCategory();
    }//GEN-LAST:event_btnDeleteSelectActionPerformed

    private void btnMoveFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveFirstActionPerformed
        // TODO add your handling code here:
        this.moveFirst();
    }//GEN-LAST:event_btnMoveFirstActionPerformed

    private void btnMovePreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMovePreviousActionPerformed
        // TODO add your handling code here:
        this.movePrevious();
    }//GEN-LAST:event_btnMovePreviousActionPerformed

    private void btnMoveNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveNextActionPerformed
        // TODO add your handling code here:
        this.moveNext();
    }//GEN-LAST:event_btnMoveNextActionPerformed

    private void btnMoveLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveLastActionPerformed
        // TODO add your handling code here:
        this.moveLast();
    }//GEN-LAST:event_btnMoveLastActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DrinkManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DrinkManagerJDialog dialog = new DrinkManagerJDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> ListBoxCategory;
    private javax.swing.JTabbedPane Tabs;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteSelect;
    private javax.swing.JButton btnMoveFirst;
    private javax.swing.JButton btnMoveLast;
    private javax.swing.JButton btnMoveNext;
    private javax.swing.JButton btnMovePrevious;
    private javax.swing.JButton btnNoSelect;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboCategory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSlider jSliderDiscount;
    private javax.swing.JLabel lblSlider;
    private javax.swing.JRadioButton rdoAvailable;
    private javax.swing.JRadioButton rdoOutOfStock;
    private javax.swing.JTable tblDrink;
    private javax.swing.JTextField txtIDDrink;
    private javax.swing.JTextField txtIDNameDrink;
    private javax.swing.JTextField txtPriceDrink;
    // End of variables declaration//GEN-END:variables
}
