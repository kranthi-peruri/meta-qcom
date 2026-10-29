SECTION = "kernel"

DESCRIPTION = "Linux ${PV} kernel for QCOM devices"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel cml1

COMPATIBLE_MACHINE = "(qcom)"

LINUX_QCOM_FIT_DTB_COMPATIBLE = "conf/machine/include/fit-dtb-compatible-linux-qcom.inc"

LINUX_VERSION ?= "7.2+7.3-rc4"

PV = "${LINUX_VERSION}+git"

KERNEL_PAHOLE ?= '${@bb.utils.contains("BBFILE_COLLECTIONS", "openembedded-layer", "1", "0", d)}'
do_configure[depends] += '${@oe.utils.vartrue("KERNEL_PAHOLE", "pahole-native:do_populate_sysroot", "", d)}'
EXTRA_OEMAKE += '${@oe.utils.vartrue("KERNEL_PAHOLE", "", "PAHOLE=false", d)}'

# tag: qcom-next-7.3-rc4-20261014
SRCREV ?= "1a0586064ee1fbcefe49b6382ee17a236c590879"

SRCBRANCH ?= "nobranch=1"
SRCBRANCH:class-devupstream ?= "branch=qcom-next"

SRC_URI = "git://github.com/qualcomm-linux/kernel.git;${SRCBRANCH};protocol=https"

# Additional kernel configs.
SRC_URI += " \
    file://configs/bsp-additions.cfg \
    file://0001-PENDING-dt-bindings-arm-qcom-Add-LeMans-Draco-EVK-co.patch \
    file://0002-PENDING-arm64-dts-qcom-Add-LeMans-Draco-EVK-SOM-devi.patch \
    file://0003-PENDING-arm64-dts-qcom-Add-LeMans-Draco-EVK-board.patch \
    file://0004-arm64-dts-qcom-lemans-draco-evk-Enable-PCIe.patch \
    file://0005-FROMLIST-PCI-qcom-Honor-IOMMU-provider-s-iommu-cells.patch \
    file://0006-arm64-dts-qcom-Enable-Lemans-Draco-connectivity.patch \
    file://0007-PENDING-arm64-dts-qcom-lemans-draco-evk-Add-LT9611UX.patch \
    file://0008-arm64-dts-qcom-lemans-draco-evk-Enable-Native-DP-sup.patch \
    file://0009-arm64-dts-qcom-lemans-draco-evk-add-video-firmware-i.patch \
    file://0010-arm64-dts-qcom-lemans-draco-evk-som-Enable-Adreno-66.patch \
    file://0011-arm64-dts-qcom-lemans-draco-evk-Add-Raspberry-Pi-7-D.patch \
    file://0012-arm64-dts-qcom-lemans-draco-evk-Add-Waveshare-8-DSI-.patch \
    file://0013-arm64-dts-qcom-Ethernet-changes-for-Lemans-Draco.patch \
    file://0014-arm64-dts-qcom-lemans-draco-evk-add-dts-changes.patch \
    file://0015-ASoC-qcom-sc8280xp-Add-driver-support-for-draco.patch \
    file://0016-Pending-Added-support-of-SD-card-for-Lemans-on-Draco.patch \
    file://0017-arm64-dts-qcom-lemans-draco-evk-Add-DLC0697-DSI-pane.patch \
    file://0018-QCLINUX-arm64-dts-qcom-lemans-Add-camera-sensor-node.patch \
    file://0019-arm64-dts-qcom-lemans-draco-evk-Enable-TPM.patch \
    file://0020-ath12k-pci-encode-qmi_instance-using-QRTR-NID-bit-la.patch \
    file://0021-arm64-dts-qcom-lemans-draco-evk-EEPROM-support.patch \
    file://0022-arm64-dts-qcom-lemans-draco-Enable-AMC6821-fan-contr.patch \
    file://0023-FROMLIST-arm64-defconfig-Enable-RPi-touchscreen-V2-r.patch \
    file://0024-FROMLIST-arm64-defconfig-Enable-Waveshare-DSI-panel-.patch \
    file://0025-QCLINUX-prune.config-Enable-the-Goodix-touchscreen-d.patch \
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
