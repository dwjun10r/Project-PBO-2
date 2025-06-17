package services;

import dao.VoucherDAO;
import models.Voucher;

import java.util.List;
import java.util.regex.Pattern;

public class VoucherService {
  private final VoucherDAO voucherDAO;

  public VoucherService(VoucherDAO voucherDAO) {
    this.voucherDAO = voucherDAO;
  }

  public List<Voucher> getAllVouchers() {
    return voucherDAO.getAllVouchers();
  }

  public Voucher getVoucherById(int id) {
    return voucherDAO.getVoucherById(id);
  }

  public boolean addVoucher(Voucher voucher) {
    if (voucher.getCode() == null || voucher.getCode().isEmpty() ||
            voucher.getDescription() == null || voucher.getDescription().isEmpty() ||
            voucher.getStartDate() == null || voucher.getStartDate().isEmpty() ||
            voucher.getEndDate() == null || voucher.getEndDate().isEmpty() ||
            voucher.getDiscount() < 0) {
      System.err.println("Validation Error: Missing required fields for Voucher or invalid discount value.");
      return false;
    }

    Pattern dateFormat = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    if (!dateFormat.matcher(voucher.getStartDate()).matches() || !dateFormat.matcher(voucher.getEndDate()).matches()) {
      System.err.println("Validation Error: Invalid date format. Use YYYY-MM-DD hh:mm:ss");
      return false;
    }

    return voucherDAO.addVoucher(voucher);
  }

  public boolean updateVoucher(Voucher voucher) {
    if (voucher.getCode() == null || voucher.getCode().isEmpty() ||
            voucher.getDescription() == null || voucher.getDescription().isEmpty() ||
            voucher.getStartDate() == null || voucher.getStartDate().isEmpty() ||
            voucher.getEndDate() == null || voucher.getEndDate().isEmpty() ||
            voucher.getDiscount() < 0) {
      System.err.println("Validation Error: Missing required fields for Voucher update or invalid discount value.");
      return false;
    }

    Pattern dateFormat = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    if (!dateFormat.matcher(voucher.getStartDate()).matches() || !dateFormat.matcher(voucher.getEndDate()).matches()) {
      System.err.println("Validation Error: Invalid date format. Use YYYY-MM-DD hh:mm:ss");
      return false;
    }

    return voucherDAO.updateVoucher(voucher);
  }

  public boolean deleteVoucher(int id) {
    return voucherDAO.deleteVoucher(id);
  }
}

