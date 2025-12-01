/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui;

import java.awt.Dialog;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import poly.cafe.util.XJDBC;
/**
 *
 * @author Duy Duc
 */
public class DRINKJDIALOG extends javax.swing.JDialog {

    private int billId;
    private DefaultTableModel tblModelCategories;
    private DefaultTableModel tblModelDrinks;

    public DRINKJDIALOG(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initTableModels(); // Khởi tạo mô hình bảng
        fillCategories(); // Đổ dữ liệu danh mục
    }

    public DRINKJDIALOG(Dialog owner, boolean modal, int billId) {
        super(owner, modal);
        initComponents();
        initTableModels();
        this.billId = billId; // sử dụng billID ở đây nếu cần
        setTitle("CHỌN NƯỚC UỐNG – BillId #" + billId);
        setLocationRelativeTo(owner); // căn giữa so với cửa sổ cha
        fillCategories();
    }
    
    private void initTableModels() {
    // Bảng DANH MỤC (bên trái)
    tblModelDrinks = new DefaultTableModel(
        new String[]{"Loại đồ uống"}, // Chỉ 1 cột
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Không cho phép chỉnh sửa
        }
    };
    tblDrinks.setModel(tblModelDrinks);
    
    // Bảng ĐỒ UỐNG (bên phải)
    tblModelCategories = new DefaultTableModel(
        new String[]{"Mã", "Tên đồ uống", "Đơn giá", "Giảm giá"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Không cho phép chỉnh sửa
        }
    };
    tblCategories.setModel(tblModelCategories);
}
    
    private void fillCategories() {
    tblModelDrinks.setRowCount(0); // Xóa dữ liệu cũ của bảng DANH MỤC (bên trái)
    try {
        String sql = "SELECT Name FROM Categories ORDER BY Name";
        ResultSet rs = XJDBC.query(sql);
        while (rs.next()) {
            tblModelDrinks.addRow(new Object[]{rs.getString("Name")});
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi tải danh mục: " + e.getMessage());
    }
}

private void fillDrinks() {
        tblModelCategories.setRowCount(0);
        int selectedRow = tblDrinks.getSelectedRow();
        if (selectedRow >= 0) {
            String categoryName = tblModelDrinks.getValueAt(selectedRow, 0).toString();
            try {
                String sql = "SELECT D.Id, D.Name, D.UnitPrice, D.Discount " +
                             "FROM Drinks D JOIN Categories C ON D.CategoryId = C.Id " +
                             "WHERE C.Name = ? ORDER BY D.Name";
                ResultSet rs = XJDBC.query(sql, categoryName);
                while (rs.next()) {
                    tblModelCategories.addRow(new Object[]{
                        rs.getString("Id"),
                        rs.getString("Name"),
                        rs.getDouble("UnitPrice"),
                        rs.getDouble("Discount")
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi tải đồ uống: " + e.getMessage());
            }
        }
    }

private void addDrinkToBillWithQuantity(int quantity) {
    int selectedRow = tblCategories.getSelectedRow();
    if (selectedRow >= 0) {
        String drinkId = tblModelCategories.getValueAt(selectedRow, 0).toString();
        try {
            String sql = "SELECT Name, UnitPrice, Discount FROM Drinks WHERE Id = ?";
            ResultSet rs = XJDBC.query(sql, drinkId);
            if (rs.next()) {
                float unitPrice = rs.getFloat("UnitPrice");
                float discount = rs.getFloat("Discount");
                String insertSql = "INSERT INTO BillDetails (BillId, DrinkId, UnitPrice, Discount, Quantity) " +
                                  "VALUES (?, ?, ?, ?, ?)";
                XJDBC.update(insertSql, billId, drinkId, unitPrice, discount, quantity);
                JOptionPane.showMessageDialog(this, "Đã thêm " + rs.getString("Name") +
                        " (" + quantity + " ly) vào hóa đơn!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm đồ uống: " + e.getMessage());
        }
    }
}

    public DRINKJDIALOG(long id) {
    if (id <= 0) {
        throw new IllegalArgumentException("ID không hợp lệ.");
    }
    // Tiếp tục tải dữ liệu vào dialog
}
    
    private void showQuantityDialogAndAddDrink() {
    int selectedRow = tblCategories.getSelectedRow();
    if (selectedRow >= 0) {
        String drinkName = tblModelCategories.getValueAt(selectedRow, 1).toString();

        String input = JOptionPane.showInputDialog(
            this,
            "Nhập số lượng cho \"" + drinkName + "\":",
            "Nhập số lượng",
            JOptionPane.PLAIN_MESSAGE
        );

        if (input != null) {
            try {
                int quantity = Integer.parseInt(input);
                if (quantity > 0) {
                    addDrinkToBillWithQuantity(quantity);
                } else {
                    JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập một số hợp lệ!");
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn một đồ uống!");
    }
}

    /**
     * Creates new form DRINKJDIALOG
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        tblCategories = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblDrinks = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblCategories.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Mã", "Tên đồ uống", "Đơn giá", "Giảm giá"
            }
        ));
        tblCategories.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCategoriesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblCategories);

        tblDrinks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Cafe"},
                {"Nước ngọt"},
                {"Nước trái cây"},
                {" Sinh tố"},
                {"Trà sữa"}
            },
            new String [] {
                "Loại đồ uống"
            }
        ));
        tblDrinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDrinksMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblDrinks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblCategoriesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCategoriesMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) { // Double-click để thêm đồ uống
            showQuantityDialogAndAddDrink();
        }
    }//GEN-LAST:event_tblCategoriesMouseClicked

    private void tblDrinksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDrinksMouseClicked
        // TODO add your handling code here:
        fillDrinks(); // Gọi fillDrinks khi nhấp vào danh mục
    }//GEN-LAST:event_tblDrinksMouseClicked

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
            java.util.logging.Logger.getLogger(DRINKJDIALOG.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DRINKJDIALOG.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DRINKJDIALOG.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DRINKJDIALOG.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DRINKJDIALOG dialog = new DRINKJDIALOG(new javax.swing.JFrame(), true);
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tblCategories;
    private javax.swing.JTable tblDrinks;
    // End of variables declaration//GEN-END:variables
}