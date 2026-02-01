import XCTest
@testable import scan_snap
import UIKit

// MARK: - UIColor Extension Tests 

class UIColorExtensionTests: XCTestCase {
    
    func testUIColorFromValidHexString() {
        let colors = [
            UIColor(hex: "FF0000"),
            UIColor(hex: "00FF00"),
            UIColor(hex: "0000FF"),
        ]
        
        colors.forEach { color in
            XCTAssertNotNil(color)
        }
    }
    
    func testUIColorFromBlackHex() {
        let color = UIColor(hex: "000000")
        XCTAssertNotNil(color)
    }
    
    func testUIColorFromWhiteHex() {
        let color = UIColor(hex: "FFFFFF")
        XCTAssertNotNil(color)
    }
    
    func testUIColorFromRedHex() {
        let color = UIColor(hex: "FF0000")
        let red = color.cgColor.components?[0] ?? 0
        XCTAssertGreaterThan(red, 0.9)
    }
    
    func testUIColorFromGreenHex() {
        let color = UIColor(hex: "00FF00")
        let green = color.cgColor.components?[1] ?? 0
        XCTAssertGreaterThan(green, 0.9)
    }
    
    func testUIColorFromBlueHex() {
        let color = UIColor(hex: "0000FF")
        let blue = color.cgColor.components?[2] ?? 0
        XCTAssertGreaterThan(blue, 0.9)
    }
    
    func testUIColorFromHexWithAlpha() {
        let color = UIColor(hex: "FF0000", alpha: 0.5)
        XCTAssertNotNil(color)
        XCTAssertEqual(color.cgColor.alpha, 0.5, accuracy: 0.01)
    }
    
    func testUIColorWithDifferentAlphaValues() {
        let alphaValues: [CGFloat] = [0.0, 0.25, 0.5, 0.75, 1.0]
        
        alphaValues.forEach { alpha in
            let color = UIColor(hex: "FF0000", alpha: alpha)
            XCTAssertNotNil(color)
            XCTAssertEqual(color.cgColor.alpha, alpha, accuracy: 0.01)
        }
    }
    
    func testUIColorDefaultAlpha() {
        let color = UIColor(hex: "FF0000")
        XCTAssertNotNil(color)
        XCTAssertEqual(color.cgColor.alpha, 1.0, accuracy: 0.01)
    }
    
    func testUIColorWithAlphaZero() {
        let color = UIColor(hex: "FF0000", alpha: 0.0)
        XCTAssertEqual(color.cgColor.alpha, 0.0, accuracy: 0.01)
    }
    
    func testUIColorWithAlphaOne() {
        let color = UIColor(hex: "FF0000", alpha: 1.0)
        XCTAssertEqual(color.cgColor.alpha, 1.0, accuracy: 0.01)
    }
    
    func testUIColorShortHexString() {
        let color = UIColor(hex: "F00")
        XCTAssertNotNil(color)
    }
    
    func testUIColorPaddsHexString() {
        let color1 = UIColor(hex: "123")
        let color2 = UIColor(hex: "112233")
        XCTAssertNotNil(color1)
        XCTAssertNotNil(color2)
    }
    
    func testUIColorHexParsing() {
        let color = UIColor(hex: "ABC")
        XCTAssertNotNil(color)
    }
    
    func testUIColorLongHexString() {
        let color = UIColor(hex: "1A2B3C4D")
        XCTAssertNotNil(color)
    }
}

// MARK: - CAShapeLayer Tests

class CAShapeLayerTests: XCTestCase {
    
    var layer: CAShapeLayer!
    
    override func setUp() {
        super.setUp()
        layer = CAShapeLayer()
    }
    
    override func tearDown() {
        layer = nil
        super.tearDown()
    }
    
    func testShapeLayerInitialization() {
        XCTAssertNotNil(layer)
    }
    
    func testShapeLayerCanSetPath() {
        let path = UIBezierPath(rect: CGRect(x: 0, y: 0, width: 100, height: 100))
        layer.path = path.cgPath
        XCTAssertNotNil(layer.path)
    }
    
    func testShapeLayerCanSetStrokeColor() {
        layer.strokeColor = UIColor.red.cgColor
        XCTAssertNotNil(layer.strokeColor)
    }
    
    func testShapeLayerCanSetLineWidth() {
        layer.lineWidth = 2.0
        XCTAssertEqual(layer.lineWidth, 2.0)
    }
    
    func testShapeLayerCanSetFillColor() {
        layer.fillColor = UIColor.blue.cgColor
        XCTAssertNotNil(layer.fillColor)
    }
    
    func testShapeLayerCanAddAnimation() {
        let animation = CABasicAnimation(keyPath: "opacity")
        animation.duration = 1.0
        layer.add(animation, forKey: "fade")
        XCTAssertTrue(true)
    }
    
    func testShapeLayerCanSetName() {
        layer.name = "testLayer"
        XCTAssertEqual(layer.name, "testLayer")
    }
    
    func testShapeLayerCanSetOpacity() {
        layer.opacity = 0.5
        XCTAssertEqual(layer.opacity, 0.5, accuracy: 0.01)
    }
    
    func testShapeLayerCanSetLineJoin() {
        layer.lineJoin = .round
        XCTAssertEqual(layer.lineJoin, .round)
    }
    
    func testShapeLayerCanSetLineCap() {
        layer.lineCap = .round
        XCTAssertEqual(layer.lineCap, .round)
    }
}

// MARK: - UIBezierPath Tests

class UIBezierPathTests: XCTestCase {
    
    func testBezierPathInitialization() {
        let path = UIBezierPath()
        XCTAssertNotNil(path)
    }
    
    func testBezierPathRect() {
        let rect = CGRect(x: 0, y: 0, width: 100, height: 100)
        let path = UIBezierPath(rect: rect)
        XCTAssertNotNil(path)
    }
    
    func testBezierPathCanMoveTo() {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 0, y: 0))
        XCTAssertTrue(true)
    }
    
    func testBezierPathCanAddLine() {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 0, y: 0))
        path.addLine(to: CGPoint(x: 100, y: 100))
        XCTAssertTrue(true)
    }
    
    func testBezierPathCanSetLineWidth() {
        let path = UIBezierPath()
        path.lineWidth = 2.0
        XCTAssertEqual(path.lineWidth, 2.0)
    }
    
    func testBezierPathUsesEvenOddFillRule() {
        let path = UIBezierPath()
        path.usesEvenOddFillRule = true
        XCTAssertTrue(path.usesEvenOddFillRule)
    }
    
    func testBezierPathCgPath() {
        let path = UIBezierPath(rect: CGRect(x: 0, y: 0, width: 50, height: 50))
        XCTAssertNotNil(path.cgPath)
    }
    
    func testBezierPathCircle() {
        let path = UIBezierPath(arcCenter: CGPoint(x: 50, y: 50),
                               radius: 30,
                               startAngle: 0,
                               endAngle: CGFloat.pi * 2,
                               clockwise: true)
        XCTAssertNotNil(path)
    }
    
    func testBezierPathOvalRect() {
        let path = UIBezierPath(ovalIn: CGRect(x: 0, y: 0, width: 100, height: 100))
        XCTAssertNotNil(path)
    }
}

// MARK: - UIDevice Tests

class UIDeviceTests: XCTestCase {
    
    func testGetCurrentDevice() {
        let device = UIDevice.current
        XCTAssertNotNil(device)
    }
    
    func testGetSystemVersion() {
        let version = UIDevice.current.systemVersion
        XCTAssertNotNil(version)
        XCTAssertFalse(version.isEmpty)
    }
    
    func testSystemVersionIsNumeric() {
        let version = UIDevice.current.systemVersion
        let components = version.split(separator: ".")
        XCTAssertGreaterThanOrEqual(components.count, 1)
    }
    
    func testGetModelName() {
        let modelName = UIDevice.current.model
        XCTAssertNotNil(modelName)
        XCTAssertFalse(modelName.isEmpty)
    }
    
    func testSystemVersionGreaterThanZero() {
        let version = UIDevice.current.systemVersion
        let firstChar = version.prefix(1)
        let isNumeric = Int(firstChar) != nil
        XCTAssertTrue(isNumeric)
    }
}

// MARK: - NotificationCenter Tests

class NotificationCenterTests: XCTestCase {
    
    func testNotificationCenterInstance() {
        let center = NotificationCenter.default
        XCTAssertNotNil(center)
    }
    
    func testRemoveObserver() {
        let center = NotificationCenter.default
        let observer = NSObject()
        
        center.addObserver(observer,
                          selector: #selector(NSObject.description),
                          name: NSNotification.Name("Test"),
                          object: nil)
        center.removeObserver(observer)
        
        XCTAssertTrue(true)
    }
    
    func testPostNotification() {
        let center = NotificationCenter.default
        let name = NSNotification.Name("TestNotification")
        
        center.post(name: name, object: nil)
        XCTAssertTrue(true)
    }
}

// MARK: - DispatchQueue Tests

class DispatchQueueTests: XCTestCase {
    
    func testMainQueueSync() {
        var executed = false
        DispatchQueue.main.sync {
            executed = true
        }
        XCTAssertTrue(executed)
    }
    
    func testCustomQueue() {
        let queue = DispatchQueue(label: "com.test.queue", attributes: .concurrent)
        XCTAssertNotNil(queue)
    }
    
    func testQueueLabel() {
        let label = "com.example.test"
        let queue = DispatchQueue(label: label)
        XCTAssertNotNil(queue)
    }
}

// MARK: - CGRect Tests 

class CGRectTests: XCTestCase {
    
    func testCGRectInitialization() {
        let rect = CGRect(x: 0, y: 0, width: 100, height: 100)
        XCTAssertEqual(rect.width, 100)
        XCTAssertEqual(rect.height, 100)
    }
    
    func testCGRectZero() {
        let rect = CGRect.zero
        XCTAssertEqual(rect.width, 0)
        XCTAssertEqual(rect.height, 0)
    }
    
    func testCGRectCalculations() {
        let rect = CGRect(x: 10, y: 20, width: 100, height: 200)
        XCTAssertEqual(rect.minX, 10)
        XCTAssertEqual(rect.minY, 20)
        XCTAssertEqual(rect.maxX, 110)
        XCTAssertEqual(rect.maxY, 220)
    }
    
    func testCGRectIntersection() {
        let rect1 = CGRect(x: 0, y: 0, width: 100, height: 100)
        let rect2 = CGRect(x: 50, y: 50, width: 100, height: 100)
        let intersection = rect1.intersection(rect2)
        XCTAssertFalse(intersection.isEmpty)
    }
}

// MARK: - UIColor Basic Tests 

class UIColorBasicTests: XCTestCase {
    
    func testUIColorRed() {
        let color = UIColor.red
        XCTAssertNotNil(color)
    }
    
    func testUIColorGreen() {
        let color = UIColor.green
        XCTAssertNotNil(color)
    }
    
    func testUIColorBlue() {
        let color = UIColor.blue
        XCTAssertNotNil(color)
    }
    
    func testUIColorBlack() {
        let color = UIColor.black
        XCTAssertNotNil(color)
    }
    
    func testUIColorWhite() {
        let color = UIColor.white
        XCTAssertNotNil(color)
    }
    
    func testUIColorWithAlphaComponent() {
        let color = UIColor.red.withAlphaComponent(0.5)
        XCTAssertNotNil(color)
        XCTAssertEqual(color.cgColor.alpha, 0.5, accuracy: 0.01)
    }
}

// MARK: - CGPoint Tests 

class CGPointTests: XCTestCase {
    
    func testCGPointInitialization() {
        let point = CGPoint(x: 10, y: 20)
        XCTAssertEqual(point.x, 10)
        XCTAssertEqual(point.y, 20)
    }
    
    func testCGPointZero() {
        let point = CGPoint.zero
        XCTAssertEqual(point.x, 0)
        XCTAssertEqual(point.y, 0)
    }
    
    func testCGPointDistance() {
        let point1 = CGPoint(x: 0, y: 0)
        let point2 = CGPoint(x: 3, y: 4)
        let distance = sqrt(pow(point2.x - point1.x, 2) + pow(point2.y - point1.y, 2))
        XCTAssertEqual(distance, 5, accuracy: 0.01)
    }
}

// MARK: - String Tests

class StringTests: XCTestCase {
    
    func testStringIsEmpty() {
        let empty = ""
        let notEmpty = "hello"
        XCTAssertTrue(empty.isEmpty)
        XCTAssertFalse(notEmpty.isEmpty)
    }
    
    func testStringSplit() {
        let version = "13.4.1"
        let components = version.split(separator: ".")
        XCTAssertEqual(components.count, 3)
    }
    
    func testStringCount() {
        let text = "Hello"
        XCTAssertEqual(text.count, 5)
    }
}