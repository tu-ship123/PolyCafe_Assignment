/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import poly.cafe.dao.CardDAO;
import poly.cafe.entity.Card;
import poly.cafe.entity.Category;
import poly.cafe.util.MsgBox;
import poly.cafe.util.XImage;

/**
 *
 * @author acer
 */
public class CardManagerJDialog extends javax.swing.JDialog {
    CardDAO dao = new CardDAO();
    int row;
    
    void init() {
        setIconImage(XImage.getAppIcon());
        setLocationRelativeTo(null);
        setTitle("DANH MỤC CARD");
        fillCardTable();
        clearForm();
    }
    /**
     * Creates new form CardManagerJDialog
     */
    public CardManagerJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        init();
    }
    
    void fillCardTable() {
    DefaultTableModel model = (DefaultTableModel) tblCard.getModel();
    model.setRowCount(0);
    try {
        List<Card> list = dao.selectAll();
        for (Card card : list) {
            String statusText;
            switch (card.getStatus()) {
                case 0:
                    statusText = "Operating";
                    break;
                case 1:
                    statusText = "Error";
                    break;
                case 2:
                    statusText = "Lose";
                    break;
                default:
                    statusText = "Unknown";
                    break;
            }
            Object[] row = {
                card.getId(),
                statusText
            };
            model.addRow(row);
        }
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi truy vấn dữ liệu!");
    }
}


private void selectAll() {
    for (int i = 0; i < tblCard.getRowCount(); i++) {
        tblCard.setValueAt(true, i, 2); // status cột thứ 2 (index 1)
    }
}

private void noSelectAll() {
    for (int i = 0; i < tblCard.getRowCount(); i++) {
        tblCard.setValueAt(false, i, 2);
    }
}
    public void deleteCard() {
    if (MsgBox.confirm(this, "Bạn đang thực hiện chức năng xoá")) {
        boolean success = false;
        for (int i = tblCard.getRowCount() - 1; i >= 0; i--) {
            Integer id = (Integer) tblCard.getValueAt(i, 0);
            Boolean checked = (Boolean) tblCard.getValueAt(i, 2);
            if (checked != null && checked) {
                try {
                    dao.delete(id);
                    success = true;
                } catch (Exception e) {
                    MsgBox.alert(this, "Lỗi: " + e.getMessage());
                }
            }
        }
        if (success) {
            MsgBox.alert(this, "Xoá thành công");
            fillCardTable();
        } else {
            MsgBox.alert(this, "Vui lòng chọn dòng để xoá");
        }
    }
}
public boolean validateForm() {
    String idText = txtCardID.getText().trim();
    if (idText.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập mã thẻ!");
        return false;
    }
    try {
        Integer.parseInt(idText);
    } catch (NumberFormatException e) {
        MsgBox.alert(this, "Mã thẻ phải là số nguyên!");
        return false;
    }

    if (!rdoOperating.isSelected() && !rdoError.isSelected() && !rdoLose.isSelected()) {
        MsgBox.alert(this, "Vui lòng chọn trạng thái!");
        return false;
    }

    return true;
}

void setForm(Card card) {
    txtCardID.setText(card.getId().toString());
    switch (card.getStatus()) {
        case 0 -> rdoOperating.setSelected(true);
        case 1 -> rdoError.setSelected(true);
        case 2 -> rdoLose.setSelected(true);
    }
}

void clearForm() {
    txtCardID.setText("");
    buttonGroup1.clearSelection();
    this.row = -1;
    this.setEditable(false);
}
    public Card getForm() {
    int id = Integer.parseInt(txtCardID.getText().trim());
    int status = rdoOperating.isSelected() ? 0 : rdoError.isSelected() ? 1 : 2;
    return new Card(id, status);
}


private void clearTableSelectionAndEditor() {
    TableCellEditor editor = tblCard.getCellEditor();
    if (editor != null) {
        editor.stopCellEditing();
    }
    tblCard.clearSelection();
    this.requestFocusInWindow();
}

void edit() {
    row = tblCard.getSelectedRow();
    try {
        Integer id = (Integer) tblCard.getValueAt(row, 0);
        Card card = dao.selectByID(id);
        if (card != null) {
            this.setForm(card);
            this.setEditable(true);
            Tabs.setSelectedIndex(1);
            clearTableSelectionAndEditor();
        }
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi truy vấn dữ liệu");
    }
}
    void insert() {
    if (!validateForm()) return;

    Card card = getForm();
    if (dao.selectByID(card.getId()) != null) {
        MsgBox.alert(this, "ID đã tồn tại");
        txtCardID.requestFocus();
        return;
    }

    try {
        dao.insert(card);
        fillCardTable();
        clearForm();
        MsgBox.alert(this, "Thêm dữ liệu thành công");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi: " + e.getMessage());
    }
}

void update() {
    if (!validateForm()) return;

    Card card = getForm();
    if (dao.selectByID(card.getId()) == null) {
        MsgBox.alert(this, "Không tồn tại ID để cập nhật");
        return;
    }

    try {
        dao.update(card);
        fillCardTable();
        clearForm();
        MsgBox.alert(this, "Cập nhật dữ liệu thành công");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi: " + e.getMessage());
    }
}

void delete() {
    String idText = txtCardID.getText().trim();
    if (idText.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập mã thẻ để xoá");
        return;
    }

    int id;
    try {
        id = Integer.parseInt(idText);
    } catch (NumberFormatException e) {
        MsgBox.alert(this, "Mã thẻ không hợp lệ");
        return;
    }

    if (dao.selectByID(id) == null) {
        MsgBox.alert(this, "Không tồn tại mã để xoá");
        return;
    }

    try {
        dao.delete(id);
        fillCardTable();
        clearForm();
        MsgBox.alert(this, "Xoá dữ liệu thành công");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi: " + e.getMessage());
    }
}

    void setEditable(boolean editable) {
    txtCardID.setEnabled(!editable);     // Mã chỉ nhập khi thêm mới
    btnAdd.setEnabled(!editable);         // Tạo mới chỉ khi không trong chế độ sửa
    btnUpdate.setEnabled(editable);          // Chỉ bật khi đang sửa
    btnDelete.setEnabled(editable);          // Chỉ bật khi đang sửa
    
    int rowCount = tblCard.getRowCount();
    btnMoveFirst.setEnabled(editable && rowCount > 0);
    btnMovePrevious.setEnabled(editable && rowCount > 0);
    btnMoveNext.setEnabled(editable && rowCount > 0);
    btnMoveLast.setEnabled(editable && rowCount > 0);
}
    void moveFirst() {
    moveTo(0);
}

void movePrevious() {
    int index = tblCard.getSelectedRow() - 1;
    moveTo(index);
}

void moveNext() {
    int index = tblCard.getSelectedRow() + 1;
    moveTo(index);
}

void moveLast() {
    int lastIndex = tblCard.getRowCount() - 1;
    moveTo(lastIndex);
}

void moveTo(int index) {
    int rowCount = tblCard.getRowCount();

    if (rowCount == 0) return;

    if (index < 0) {
        moveLast();
    } else if (index >= rowCount) {
        moveFirst();
    } else {
        tblCard.clearSelection();
        tblCard.setRowSelectionInterval(index, index);
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
        tblCard = new javax.swing.JTable();
        btnSelectAll = new javax.swing.JButton();
        btnNoSelect = new javax.swing.JButton();
        btnDeleteSelect = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCardID = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        rdoOperating = new javax.swing.JRadioButton();
        rdoError = new javax.swing.JRadioButton();
        rdoLose = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnMoveFirst = new javax.swing.JButton();
        btnMovePrevious = new javax.swing.JButton();
        btnMoveNext = new javax.swing.JButton();
        btnMoveLast = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        Tabs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabsMouseClicked(evt);
            }
        });

        tblCard.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Mã thẻ", "Trạng thái", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblCardMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblCard);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSelectAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteSelect)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeleteSelect)
                    .addComponent(btnNoSelect)
                    .addComponent(btnSelectAll))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        Tabs.addTab("DANH SÁCH", jPanel1);

        jLabel1.setText("Mã thẻ");

        jLabel2.setText("Trạng thái");

        buttonGroup1.add(rdoOperating);
        rdoOperating.setText("Operating");
        rdoOperating.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoOperatingActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoError);
        rdoError.setText("Error");

        buttonGroup1.add(rdoLose);
        rdoLose.setText("Lose");

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(rdoOperating)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoError)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoLose))
                            .addComponent(txtCardID, javax.swing.GroupLayout.PREFERRED_SIZE, 619, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(0, 17, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
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
                        .addComponent(btnMoveLast, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCardID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdoOperating)
                            .addComponent(rdoError)
                            .addComponent(rdoLose))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnMoveLast)
                            .addComponent(btnMoveNext)
                            .addComponent(btnMovePrevious)
                            .addComponent(btnMoveFirst)
                            .addComponent(btnClear)
                            .addComponent(btnDelete)
                            .addComponent(btnUpdate)
                            .addComponent(btnAdd))
                        .addContainerGap())))
        );

        Tabs.addTab("BIỂU MẪU", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Tabs)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Tabs)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rdoOperatingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoOperatingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdoOperatingActionPerformed

    private void TabsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabsMouseClicked
        // TODO add your handling code here:
        clearTableSelectionAndEditor();
    }//GEN-LAST:event_TabsMouseClicked

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed
        // TODO add your handling code here:
        selectAll();
    }//GEN-LAST:event_btnSelectAllActionPerformed

    private void tblCardMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCardMousePressed
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            edit();
        }
    }//GEN-LAST:event_tblCardMousePressed

    private void btnNoSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoSelectActionPerformed
        // TODO add your handling code here:
        noSelectAll();
    }//GEN-LAST:event_btnNoSelectActionPerformed

    private void btnDeleteSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectActionPerformed
        // TODO add your handling code here:
        deleteCard();
    }//GEN-LAST:event_btnDeleteSelectActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        insert();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

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
            java.util.logging.Logger.getLogger(CardManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CardManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CardManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CardManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CardManagerJDialog dialog = new CardManagerJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton rdoError;
    private javax.swing.JRadioButton rdoLose;
    private javax.swing.JRadioButton rdoOperating;
    private javax.swing.JTable tblCard;
    private javax.swing.JTextField txtCardID;
    // End of variables declaration//GEN-END:variables
}
