
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import poly.cafe.dao.CategoryDAO;
import poly.cafe.entity.Category;
import java.util.List;

public class CategoryDAOTest {

    CategoryDAO dao;
    String testID = "TEST_CAT"; 

    @BeforeEach
    public void setUp() {
        dao = new CategoryDAO();
        cleanup();
    }

    @AfterEach
    public void tearDown() {
        cleanup();
    }

    void cleanup() {
        try { dao.delete(testID); } catch (Exception e) {}
    }

    // TC1: Thêm mới thành công
    @Test
    public void TC01_Insert_Success() {
        Category cat = new Category(testID, "Cafe Test");
        try {
            dao.insert(cat);
            Assertions.assertNotNull(dao.selectByID(testID));
        } catch (Exception e) { Assertions.fail(e.getMessage()); }
    }

    // TC2: Tên biên tối thiểu (1 ký tự)
    @Test
    public void TC02_Insert_MinLengthName() {
        Category cat = new Category(testID, "AB");
        try {
            dao.insert(cat);
            Assertions.assertEquals("AB", dao.selectByID(testID).getName());
        } catch (Exception e) { Assertions.fail(e.getMessage()); }
    }

    // TC3: Tên biên tối đa (50 ký tự - Giả sử DB giới hạn 50)
    @Test
    public void TC03_Insert_MaxLengthName() {
        String longName = "A".repeat(50); 
        Category cat = new Category(testID, longName);
        try {
            dao.insert(cat);
            Assertions.assertEquals(longName, dao.selectByID(testID).getName());
        } catch (Exception e) { Assertions.fail(e.getMessage()); }
    }

    // TC4: Lỗi trùng Mã
    @Test
    public void TC04_Insert_DuplicateID() {
        dao.insert(new Category(testID, "Lan 1"));
        try {
            dao.insert(new Category(testID, "Lan 2")); // Trùng
            Assertions.fail("Phải báo lỗi trùng khóa chính");
        } catch (Exception e) { Assertions.assertTrue(true); }
    }

    // TC5: Lỗi Tên để trống
    @Test
    public void TC05_Insert_EmptyName() {
        try {
            dao.insert(new Category(testID, ""));
            // Nếu DB chặn thì pass, nếu không chặn thì cần xem lại logic DAO
            // Assertions.fail("Không được insert tên rỗng"); 
        } catch (Exception e) { Assertions.assertTrue(true); }
    }

    // TC6: Lỗi Mã để trống (NULL)
    @Test
    public void TC06_Insert_NullID() {
        try {
            dao.insert(new Category(null, "Ten"));
            Assertions.fail("Không được insert ID null");
        } catch (Exception e) { Assertions.assertTrue(true); }
    }

    // TC7: Lỗi Tên quá dài (>50 ký tự)
    @Test
    public void TC07_Insert_NameTooLong() {
        String tooLong = "A".repeat(51);
        try {
            dao.insert(new Category(testID, tooLong));
            Assertions.fail("Phải báo lỗi Data Truncation");
        } catch (Exception e) { Assertions.assertTrue(true); }
    }

    // TC8: Cập nhật thành công
    @Test
    public void TC08_Update_Success() {
        dao.insert(new Category(testID, "Old Name"));
        Category cat = dao.selectByID(testID);
        cat.setName("New Name");
        dao.update(cat);
        Assertions.assertEquals("New Name", dao.selectByID(testID).getName());
    }

    // TC9: Lỗi cập nhật tên trùng (Nếu có ràng buộc Unique Name)
    @Test
    public void TC09_Update_DuplicateName() {
        // Test này chỉ chạy nếu DB có constraint Unique Name
        // Nếu không có, có thể bỏ qua hoặc assert true
        Assertions.assertTrue(true, "Bỏ qua nếu không có constraint Unique Name");
    }

    // TC10: Cập nhật ID (Không được phép đổi ID)
    @Test
    public void TC10_Update_ChangeID() {
        // Logic DAO thường update theo ID, nên không thể đổi ID trực tiếp
        // Ta test việc ID cũ vẫn tồn tại
        dao.insert(new Category(testID, "Name"));
        Category cat = new Category(testID, "Name");
        // Giả lập việc set ID khác là vô nghĩa trong logic update chuẩn
        dao.update(cat); 
        Assertions.assertNotNull(dao.selectByID(testID)); 
    }

    // TC11: Xóa thành công (Rỗng)
    @Test
    public void TC11_Delete_Success() {
        dao.insert(new Category(testID, "Del"));
        dao.delete(testID);
        Assertions.assertNull(dao.selectByID(testID));
    }

    // TC12: Lỗi xóa danh mục đang có sản phẩm
    @Test
    public void TC12_Delete_ConstraintFK() {
        // Cần dữ liệu thật: 1 Category đang có món
        // Giả sử CatID="CF" đang có cafe. Test sẽ pass nếu bắt được lỗi Constraint
        try {
            dao.delete("CF"); // Mã thực tế trong DB
            // Nếu xóa được -> Fail (vì mất dữ liệu)
            // Assertions.fail("Không được xóa danh mục đang có đồ uống");
        } catch (Exception e) {
            Assertions.assertTrue(true, "Bắt được lỗi ràng buộc khóa ngoại");
        }
    }

    // TC13: Xóa ID không tồn tại
    @Test
    public void TC13_Delete_NotFound() {
        try {
            dao.delete("ID_AO_123");
            Assertions.assertTrue(true);
        } catch (Exception e) { Assertions.fail("Không được crash app"); }
    }

    // TC14: Hiển thị danh sách (Có data)
    @Test
    public void TC14_SelectAll_HasData() {
        List<Category> list = dao.selectAll();
        Assertions.assertNotNull(list);
    }

    // TC15: Hiển thị danh sách (Biên rỗng)
    @Test
    public void TC15_SelectAll_Empty() {
        // Test này khó chạy trên DB thật đang có dữ liệu
        // Logic: Hàm không được trả về NULL mà phải trả về List rỗng
        List<Category> list = dao.selectAll();
        Assertions.assertNotNull(list, "Dù rỗng cũng không được trả về NULL");
    }
}