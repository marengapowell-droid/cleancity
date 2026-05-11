import java.util.ArrayList;
import java.util.Scanner;

// ========== COLORS ==========
class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";
}

// ========== ABSTRACT USER CLASS ==========
abstract class User {
    protected int userId;
    protected String name;
    protected String email;
    protected String role;
    
    public User(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
    
    public abstract void displayMenu();
    public String getName() { return name; }
    public int getUserId() { return userId; }
}

// ========== PICKUP STATUS ENUM ==========
enum PickupStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
}

// ========== PICKUP CLASS ==========
class Pickup {
    private int pickupId;
    private String residentName;
    private String wasteType;
    private String date;
    private String time;
    private String location;
    private PickupStatus status;
    private static int nextId = 1;
    
    public Pickup(int id, String residentName, String wasteType, String date, String time, String location) {
        this.pickupId = id;
        this.residentName = residentName;
        this.wasteType = wasteType;
        this.date = date;
        this.time = time;
        this.location = location;
        this.status = PickupStatus.SCHEDULED;
        if (id >= nextId) nextId = id + 1;
    }
    
    public int getPickupId() { return pickupId; }
    public PickupStatus getStatus() { return status; }
    public void setStatus(PickupStatus status) { this.status = status; }
    public String getResidentName() { return residentName; }
    public String getWasteType() { return wasteType; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public static int getNextId() { return nextId; }
    
    public void display() {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│ 🗑️ " + wasteType + " PICKUP");
        System.out.println("│ 📍 " + location);
        System.out.println("│ 📅 " + date + " at " + time);
        System.out.println("│ 🏷️ Status: " + status);
        System.out.println("│ 👤 Resident: " + residentName);
        System.out.println("└─────────────────────────────────────────────┘");
    }
}

// ========== REPORT CLASS ==========
class Report {
    private int reportId;
    private String reporterName;
    private String issueType;
    private String location;
    private String description;
    private String status;
    private static int nextId = 101;
    
    public Report(int id, String reporterName, String issueType, String location, String description) {
        this.reportId = id;
        this.reporterName = reporterName;
        this.issueType = issueType;
        this.location = location;
        this.description = description;
        this.status = "PENDING";
        if (id >= nextId) nextId = id + 1;
    }
    
    public int getReportId() { return reportId; }
    public boolean isResolved() { return status.equals("RESOLVED"); }
    public void resolve() { this.status = "RESOLVED"; }
    public String getStatus() { return status; }
    public String getReporterName() { return reporterName; }
    public static int getNextId() { return nextId; }
    
    public void display() {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│ ⚠️ " + issueType);
        System.out.println("│ 📍 " + location);
        System.out.println("│ 📝 " + description);
        System.out.println("│ 🏷️ Status: " + status);
        System.out.println("│ 👤 Reporter: " + reporterName);
        System.out.println("└─────────────────────────────────────────────┘");
    }
}

// ========== RESIDENT CLASS ==========
class Resident extends User {
    private ArrayList<Pickup> myPickups;
    private ArrayList<Report> myReports;
    
    public Resident(int userId, String name, String email) {
        super(userId, name, email, "RESIDENT");
        myPickups = new ArrayList<>();
        myReports = new ArrayList<>();
    }
    
    @Override
    public void displayMenu() {
        System.out.println("\n" + Colors.CYAN + "╔════════════════════════════════╗" + Colors.RESET);
        System.out.println(Colors.CYAN + "║    👤 RESIDENT MENU            ║" + Colors.RESET);
        System.out.println(Colors.CYAN + "╠════════════════════════════════╣" + Colors.RESET);
        System.out.println(Colors.CYAN + "║" + Colors.RESET + " 1. Request Pickup              " + Colors.CYAN + "║" + Colors.RESET);
        System.out.println(Colors.CYAN + "║" + Colors.RESET + " 2. Report Issue                " + Colors.CYAN + "║" + Colors.RESET);
        System.out.println(Colors.CYAN + "║" + Colors.RESET + " 3. View My Pickups             " + Colors.CYAN + "║" + Colors.RESET);
        System.out.println(Colors.CYAN + "║" + Colors.RESET + " 4. View My Reports             " + Colors.CYAN + "║" + Colors.RESET);
        System.out.println(Colors.CYAN + "║" + Colors.RESET + " 5. Logout                      " + Colors.CYAN + "║" + Colors.RESET);
        System.out.println(Colors.CYAN + "╚════════════════════════════════╝" + Colors.RESET);
    }
    
    public void requestPickup(Scanner scanner, Admin admin) {
        System.out.println("\n📦 NEW PICKUP REQUEST");
        System.out.print("Waste type (Plastic/Organic/Electronic/General): ");
        String wasteType = scanner.nextLine();
        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Time (e.g., 9AM-11AM): ");
        String time = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        
        int newId = Pickup.getNextId();
        Pickup p = new Pickup(newId, this.name, wasteType, date, time, location);
        myPickups.add(p);
        admin.addPickup(p);
        System.out.println(Colors.GREEN + "✅ Pickup requested! ID: " + p.getPickupId() + Colors.RESET);
    }
    
    public void reportIssue(Scanner scanner, Admin admin) {
        System.out.println("\n⚠️ NEW ISSUE REPORT");
        System.out.print("Issue type (Missed Pickup/Overflowing Bin/Illegal Dumping): ");
        String issueType = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        
        int newId = Report.getNextId();
        Report r = new Report(newId, this.name, issueType, location, description);
        myReports.add(r);
        admin.addReport(r);
        System.out.println(Colors.GREEN + "✅ Issue reported! ID: RPT-" + r.getReportId() + Colors.RESET);
    }
    
    public void viewPickups() {
        if (myPickups.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No pickups yet" + Colors.RESET);
            return;
        }
        System.out.println("\n📋 YOUR PICKUPS:");
        for (Pickup p : myPickups) p.display();
    }
    
    public void viewReports() {
        if (myReports.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No reports yet" + Colors.RESET);
            return;
        }
        System.out.println("\n📋 YOUR REPORTS:");
        for (Report r : myReports) r.display();
    }
    
    public ArrayList<Pickup> getPickups() { return myPickups; }
    public ArrayList<Report> getReports() { return myReports; }
}

// ========== COLLECTOR CLASS ==========
class Collector extends User {
    private ArrayList<Pickup> assignedPickups;
    
    public Collector(int userId, String name, String email) {
        super(userId, name, email, "COLLECTOR");
        assignedPickups = new ArrayList<>();
    }
    
    @Override
    public void displayMenu() {
        System.out.println("\n" + Colors.BLUE + "╔════════════════════════════════╗" + Colors.RESET);
        System.out.println(Colors.BLUE + "║    🚛 COLLECTOR MENU           ║" + Colors.RESET);
        System.out.println(Colors.BLUE + "╠════════════════════════════════╣" + Colors.RESET);
        System.out.println(Colors.BLUE + "║" + Colors.RESET + " 1. View Assigned Pickups       " + Colors.BLUE + "║" + Colors.RESET);
        System.out.println(Colors.BLUE + "║" + Colors.RESET + " 2. Update Pickup Status        " + Colors.BLUE + "║" + Colors.RESET);
        System.out.println(Colors.BLUE + "║" + Colors.RESET + " 3. Logout                      " + Colors.BLUE + "║" + Colors.RESET);
        System.out.println(Colors.BLUE + "╚════════════════════════════════╝" + Colors.RESET);
    }
    
    public void assignPickup(Pickup p) { assignedPickups.add(p); }
    
    public void viewPickups() {
        if (assignedPickups.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No assigned pickups" + Colors.RESET);
            return;
        }
        System.out.println("\n🚛 ASSIGNED PICKUPS:");
        for (Pickup p : assignedPickups) p.display();
    }
    
    public void updateStatus(Scanner scanner) {
        if (assignedPickups.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No pickups to update" + Colors.RESET);
            return;
        }
        viewPickups();
        System.out.print("Enter Pickup ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        for (Pickup p : assignedPickups) {
            if (p.getPickupId() == id) {
                System.out.println("Current Status: " + p.getStatus());
                System.out.println("1. SCHEDULED    2. IN_PROGRESS");
                System.out.println("3. COMPLETED    4. CANCELLED");
                System.out.print("Choose (1-4): ");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch(choice) {
                    case 1: p.setStatus(PickupStatus.SCHEDULED); break;
                    case 2: p.setStatus(PickupStatus.IN_PROGRESS); break;
                    case 3: p.setStatus(PickupStatus.COMPLETED); break;
                    case 4: p.setStatus(PickupStatus.CANCELLED); break;
                    default: System.out.println(Colors.RED + "❌ Invalid choice!" + Colors.RESET); return;
                }
                System.out.println(Colors.GREEN + "✅ Status updated to: " + p.getStatus() + Colors.RESET);
                return;
            }
        }
        System.out.println(Colors.RED + "❌ Pickup not found" + Colors.RESET);
    }
}

// ========== ADMIN CLASS ==========
class Admin extends User {
    private ArrayList<Report> allReports;
    private ArrayList<Pickup> allPickups;
    private ArrayList<Resident> allResidents;
    
    public Admin(int userId, String name, String email) {
        super(userId, name, email, "ADMIN");
        allReports = new ArrayList<>();
        allPickups = new ArrayList<>();
        allResidents = new ArrayList<>();
    }
    
    public void addReport(Report r) { allReports.add(r); }
    public void addPickup(Pickup p) { allPickups.add(p); }
    public void addResident(Resident r) { allResidents.add(r); }
    public ArrayList<Pickup> getAllPickups() { return allPickups; }
    public ArrayList<Report> getAllReports() { return allReports; }
    
    @Override
    public void displayMenu() {
        System.out.println("\n" + Colors.PURPLE + "╔════════════════════════════════╗" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║    👑 ADMIN DASHBOARD          ║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "╠════════════════════════════════╣" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║" + Colors.RESET + " 1. View All Pickups           " + Colors.PURPLE + "║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║" + Colors.RESET + " 2. View All Reports           " + Colors.PURPLE + "║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║" + Colors.RESET + " 3. Resolve Report             " + Colors.PURPLE + "║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║" + Colors.RESET + " 4. Assign Pickup to Collector " + Colors.PURPLE + "║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║" + Colors.RESET + " 5. View Statistics            " + Colors.PURPLE + "║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "║" + Colors.RESET + " 6. Logout                     " + Colors.PURPLE + "║" + Colors.RESET);
        System.out.println(Colors.PURPLE + "╚════════════════════════════════╝" + Colors.RESET);
    }
    
    public void viewAllPickups() {
        if (allPickups.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No pickups in system" + Colors.RESET);
            return;
        }
        System.out.println("\n📦 ALL PICKUPS IN SYSTEM:");
        for (Pickup p : allPickups) p.display();
    }
    
    public void viewAllReports() {
        if (allReports.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No reports in system" + Colors.RESET);
            return;
        }
        System.out.println("\n⚠️ ALL REPORTS IN SYSTEM:");
        for (Report r : allReports) r.display();
    }
    
    public void resolveReport(Scanner scanner) {
        if (allReports.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No reports to resolve" + Colors.RESET);
            return;
        }
        viewAllReports();
        System.out.print("\nEnter Report ID to resolve: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        for (Report r : allReports) {
            if (r.getReportId() == id) {
                r.resolve();
                System.out.println(Colors.GREEN + "✅ Report resolved!" + Colors.RESET);
                return;
            }
        }
        System.out.println(Colors.RED + "❌ Report not found" + Colors.RESET);
    }
    
    public void assignPickupToCollector(Scanner scanner, ArrayList<Collector> collectors) {
        if (allPickups.isEmpty()) {
            System.out.println(Colors.YELLOW + "📭 No pickups to assign" + Colors.RESET);
            return;
        }
        viewAllPickups();
        System.out.print("\nEnter Pickup ID: ");
        int pid = scanner.nextInt();
        scanner.nextLine();
        
        Pickup target = null;
        for (Pickup p : allPickups) {
            if (p.getPickupId() == pid) { target = p; break; }
        }
        if (target == null) {
            System.out.println(Colors.RED + "❌ Pickup not found" + Colors.RESET);
            return;
        }
        
        System.out.println("\nCollectors:");
        for (Collector c : collectors) {
            System.out.println("  ID " + c.getUserId() + " - " + c.getName());
        }
        System.out.print("Enter Collector ID: ");
        int cid = scanner.nextInt();
        scanner.nextLine();
        
        for (Collector c : collectors) {
            if (c.getUserId() == cid) {
                c.assignPickup(target);
                System.out.println(Colors.GREEN + "✅ Assigned!" + Colors.RESET);
                return;
            }
        }
        System.out.println(Colors.RED + "❌ Collector not found" + Colors.RESET);
    }
    
    public void showStatistics() {
        int resolved = 0;
        for (Report r : allReports) if (r.isResolved()) resolved++;
        int rate = allReports.isEmpty() ? 0 : (resolved * 100 / allReports.size());
        
        System.out.println("\n📊 STATISTICS:");
        System.out.println("   Total Pickups: " + allPickups.size());
        System.out.println("   Total Reports: " + allReports.size());
        System.out.println("   Resolved Reports: " + resolved);
        System.out.println("   Resolution Rate: " + rate + "%");
        System.out.println("   Active Residents: " + allResidents.size());
    }
}

// ========== MAIN APPLICATION ==========
public class CleanCity {
    private static Scanner scanner = new Scanner(System.in);
    private static Resident currentResident;
    private static Admin admin;
    private static ArrayList<Collector> collectors = new ArrayList<>();
    private static ArrayList<Resident> residents = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        System.out.println(Colors.CYAN + "╔════════════════════════════════════════╗" + Colors.RESET);
        System.out.println(Colors.CYAN + "║   🌍 CLEANCITY - SMART WASTE         ║" + Colors.RESET);
        System.out.println(Colors.CYAN + "║         MANAGEMENT PLATFORM           ║" + Colors.RESET);
        System.out.println(Colors.CYAN + "╚════════════════════════════════════════╝" + Colors.RESET);
        System.out.println("   Powell Njenga | SCNI/01173/2022\n");
        
        // Initialize admin
        admin = new Admin(301, "Admin", "admin@cleancity.com");
        
        // Create demo data
        Resident demo = new Resident(1, "Powell Njenga", "powell@cleancity.com");
        residents.add(demo);
        currentResident = demo;
        admin.addResident(demo);
        
        Collector collector = new Collector(201, "John Kamau", "john@cleancity.com");
        collectors.add(collector);
        
        Pickup p1 = new Pickup(1, "Powell", "Plastic", "2026-04-20", "9AM-11AM", "CBD Nairobi");
        admin.addPickup(p1);
        
        Report r1 = new Report(101, "Powell", "Overflowing Bin", "Westlands", "Bin full for 2 weeks");
        admin.addReport(r1);
        
        System.out.println(Colors.GREEN + "✅ System Ready!" + Colors.RESET);
        System.out.println("   Resident: Powell | Collector ID: 201 | Admin PW: admin123\n");
        
        mainMenu();
    }
    
    private static void mainMenu() {
        while (true) {
            System.out.println(Colors.CYAN + "╔════════════════════════════════════════╗" + Colors.RESET);
            System.out.println(Colors.CYAN + "║              MAIN MENU                  ║" + Colors.RESET);
            System.out.println(Colors.CYAN + "╠════════════════════════════════════════╣" + Colors.RESET);
            System.out.println(Colors.CYAN + "║" + Colors.RESET + "  1. Resident Login                      " + Colors.CYAN + "║" + Colors.RESET);
            System.out.println(Colors.CYAN + "║" + Colors.RESET + "  2. Collector Login                     " + Colors.CYAN + "║" + Colors.RESET);
            System.out.println(Colors.CYAN + "║" + Colors.RESET + "  3. Admin Login                         " + Colors.CYAN + "║" + Colors.RESET);
            System.out.println(Colors.CYAN + "║" + Colors.RESET + "  4. Exit                                " + Colors.CYAN + "║" + Colors.RESET);
            System.out.println(Colors.CYAN + "╚════════════════════════════════════════╝" + Colors.RESET);
            System.out.print("   Choose option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            if (choice == 1) residentLogin();
            else if (choice == 2) collectorLogin();
            else if (choice == 3) adminLogin();
            else if (choice == 4) {
                System.out.println(Colors.GREEN + "👋 Goodbye! Keep our city clean!" + Colors.RESET);
                System.exit(0);
            }
            else System.out.println(Colors.RED + "❌ Invalid" + Colors.RESET);
        }
    }
    
    private static void residentLogin() {
        System.out.print("\n👤 Enter name (Enter for demo): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) {
            currentResident = new Resident(residents.size() + 1, name, name + "@cleancity.com");
            residents.add(currentResident);
            admin.addResident(currentResident);
        }
        System.out.println(Colors.GREEN + "✅ Welcome " + currentResident.getName() + "!" + Colors.RESET);
        
        while (true) {
            currentResident.displayMenu();
            System.out.print("   Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            if (choice == 1) {
                currentResident.requestPickup(scanner, admin);
            }
            else if (choice == 2) {
                currentResident.reportIssue(scanner, admin);
            }
            else if (choice == 3) currentResident.viewPickups();
            else if (choice == 4) currentResident.viewReports();
            else if (choice == 5) {
                System.out.println("👋 Logging out...");
                return;
            }
            else System.out.println(Colors.RED + "❌ Invalid" + Colors.RESET);
        }
    }
    
    private static void collectorLogin() {
        System.out.print("\n🚛 Enter Collector ID (201): ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        for (Collector c : collectors) {
            if (c.getUserId() == id) {
                System.out.println(Colors.GREEN + "✅ Welcome " + c.getName() + "!" + Colors.RESET);
                while (true) {
                    c.displayMenu();
                    System.out.print("   Choose option: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice == 1) c.viewPickups();
                    else if (choice == 2) c.updateStatus(scanner);
                    else if (choice == 3) {
                        System.out.println("👋 Logging out...");
                        return;
                    }
                    else System.out.println(Colors.RED + "❌ Invalid" + Colors.RESET);
                }
            }
        }
        System.out.println(Colors.RED + "❌ Collector not found" + Colors.RESET);
    }
    
    private static void adminLogin() {
        System.out.print("\n👑 Password: ");
        String pw = scanner.nextLine();
        
        if (pw.equals("admin123")) {
            System.out.println(Colors.GREEN + "✅ Welcome Admin!" + Colors.RESET);
            while (true) {
                admin.displayMenu();
                System.out.print("   Choose option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1) admin.viewAllPickups();
                else if (choice == 2) admin.viewAllReports();
                else if (choice == 3) admin.resolveReport(scanner);
                else if (choice == 4) admin.assignPickupToCollector(scanner, collectors);
                else if (choice == 5) admin.showStatistics();
                else if (choice == 6) {
                    System.out.println("👋 Logging out...");
                    return;
                }
                else System.out.println(Colors.RED + "❌ Invalid" + Colors.RESET);
            }
        } else {
            System.out.println(Colors.RED + "❌ Wrong password!" + Colors.RESET);
        }
    }
}