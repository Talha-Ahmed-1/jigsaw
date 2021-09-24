package jigsaw
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig, WishboneDevice, WishboneHost}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled
import jigsaw.peripherals.spi._

class SpiHarness(implicit val config: WishboneConfig, spiConfig:Config) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new WBRequest()))
    val rsp = Decoupled(new WBResponse())

    // master spi interfaces
    val cs_n = Output(Bool())
    val sclk = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())

  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val spi = Module(new Spi(new WBRequest(), new WBResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.wbMasterTransmitter <> deviceAdapter.io.wbMasterReceiver
  hostAdapter.io.wbSlaveReceiver <> deviceAdapter.io.wbSlaveTransmitter

  spi.io.req <> deviceAdapter.io.reqOut
  spi.io.rsp <> deviceAdapter.io.rspIn


    io.cs_n := spi.io.cs_n
    io.sclk := spi.io.sclk
    io.mosi := spi.io.mosi
    
    spi.io.miso := io.miso
}

object SpiHarnessDriver extends App {
  implicit val config = WishboneConfig(32,32)
  implicit val spiConfig = Config()
  println(ChiselStage.emitVerilog(new SpiHarness()))
}
