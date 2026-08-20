SECTION = "kernel"

DESCRIPTION = "Linux ${PV} kernel for QCOM devices"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel cml1

COMPATIBLE_MACHINE = "(qcom)"

LINUX_QCOM_FIT_DTB_COMPATIBLE = "conf/machine/include/fit-dtb-compatible-linux-qcom.inc"

LINUX_VERSION ?= "7.1"

PV = "${LINUX_VERSION}+git"

# tag: qcom-next-7.1-20260628
SRCREV ?= "19b282f417584cfe14ce6a262122c51553d026ec"

SRCBRANCH ?= "nobranch=1"
SRCBRANCH:class-devupstream ?= "branch=qcom-next"

SRC_URI = "git://github.com/qualcomm-linux/kernel.git;${SRCBRANCH};protocol=https"

# Additional kernel configs.
SRC_URI += " \
    file://configs/bsp-additions.cfg \
    file://0001-PENDING-arm64-dts-qcom-talos-evk-add-QPS615-m.2-ethe.patch \
    file://0001-arm64-dts-qcom-add-talos-iot-som-platform.patch \
    file://0002-arm64-dts-qcom-add-talos-lyra-evk-board.patch \
    file://0003-dt-bindings-arm-qcom-add-talos-lyra-evk-board.patch \
    file://0004-arm64-dts-qcom-talos-lyra-evk-Enable-USB-controllers.patch \
    file://0005-arm64-dts-qcom-Enable-UFS-support-for-Talos-IoT-SoM.patch \
    file://0006-arm64-dts-qcom-Enable-eMMC-support-for-Talos-IoT-SoM.patch \
    file://0007-arm64-dts-qcom-talos-lyra-evk-Enable-Native-DP.patch \
    file://0008-arm64-dts-qcom-talos-lyra-evk-Add-I2C-GPIO-expanders.patch \
    file://0009-arm64-dts-qcom-talos-lyra-evk-Enable-PCIe-Support.patch \
    file://0010-arm64-dts-qcom-talos-lyra-evk-Enable-M.2-Key-E-Colog.patch \
    file://0011-arm64-dts-qcom-talos-lyra-evk-Enable-GPU.patch \
    file://0012-drm-bridge-Add-new-atomic_create_state-callback.patch \
    file://0013-drm-atomic-state-helper-Add-drm_atomic_helper_bridge.patch \
    file://0014_1-drm-bridge-lontium-add-LT9611C-EX-UXD-DSI-to-HDMI-br.patch \
    file://0014_2-drm-bridge-add-of_drm_get_bridge_by_endpoint.patch \
    file://0015-arm64-dts-qcom-talos-lyra-evk-Add-LT9611UXD-HDMI-bri.patch \
    file://0016-arm64-dts-msm-Enable-sdcard-support-for-Talos-IoT-So.patch \
    file://0017-arm64-dts-qcom-Add-Camera-DT-changes-for-Talos-Lyra-.patch \
    file://0018-arm64-dts-qcom-Add-IMX577-Camera-DT-changes-for-Talo.patch \
    file://0019-arm64-dts-qcom-talos-lyra-evk-Enable-TPM-ST33.patch \
    file://0020-arm64-dts-qcom-add-Talos-Lyra-EVK-DTS-for-ethernet-c.patch \
    file://0021-linux-qcom-next-Ethernet-driver-fix.patch \
"

# To build tip of qcom-next branch set preferred
# virtual/kernel provider to 'linux-qcom-next-upstream'
BBCLASSEXTEND = "devupstream:target"
PN:class-devupstream = "linux-qcom-next-upstream"
SRCREV:class-devupstream ?= "${AUTOREV}"

S = "${UNPACKDIR}/${BP}"

KBUILD_DEFCONFIG ?= "defconfig"
KBUILD_DEFCONFIG:qcom-armv7a = "qcom_defconfig"

KBUILD_CONFIG_EXTRA = "${@bb.utils.contains('DISTRO_FEATURES', 'hardened', '${S}/kernel/configs/hardening.config', '', d)}"
KBUILD_CONFIG_EXTRA:append:aarch64 = " ${S}/arch/arm64/configs/prune.config"
KBUILD_CONFIG_EXTRA:append:aarch64 = " ${S}/arch/arm64/configs/qcom.config"
KBUILD_CONFIG_EXTRA:append = " ${@oe.utils.vartrue('DEBUG_BUILD', '${S}/kernel/configs/debug.config', '', d)}"
KBUILD_CONFIG_EXTRA:append:aarch64 = " ${@oe.utils.vartrue('DEBUG_BUILD', '${S}/arch/arm64/configs/qcom_debug.config', '', d)}"

do_configure:prepend() {
    # Use a copy of the 'defconfig' from the actual repo to merge fragments
    cp ${S}/arch/${ARCH}/configs/${KBUILD_DEFCONFIG} ${B}/.config

    # Merge fragment for QCOM value add features
    ${S}/scripts/kconfig/merge_config.sh -m -O ${B} ${B}/.config ${KBUILD_CONFIG_EXTRA} ${@" ".join(find_cfgs(d))}
}
