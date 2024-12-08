public class Pothole {

    private String id;
    private double latitude;
    private double longitude;
    private float acceleration;
    private String detectedAt;
    private float tiltAngle;
    private String status;
    private String condition;
    private float depth;
    private String description;
    private String analysisResult;

    // Constructor
    public Pothole(String id, double latitude, double longitude, float acceleration,
                   String detectedAt, float tiltAngle, String status, String condition,
                   float depth, String description, String analysisResult) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acceleration = acceleration;
        this.detectedAt = detectedAt;
        this.tiltAngle = tiltAngle;
        this.status = status;
        this.condition = condition;
        this.depth = depth;
        this.description = description;
        this.analysisResult = analysisResult;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public String getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(String detectedAt) {
        this.detectedAt = detectedAt;
    }

    public float getTiltAngle() {
        return tiltAngle;
    }

    public void setTiltAngle(float tiltAngle) {
        this.tiltAngle = tiltAngle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }
}
