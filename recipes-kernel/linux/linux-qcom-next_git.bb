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
"

# Monaco Draco EVK board support (pending upstream).
SRC_URI += " \
    file://patches/0001-dt-bindings-arm-qcom-add-monaco-draco-evk-compatible.patch \
    file://patches/0002-arm64-dts-qcom-add-monaco-draco-ac-evk-som-device-tr.patch \
    file://patches/0003-arm64-dts-qcom-add-monaco-draco-evk-board.patch \
    file://patches/0004-arm64-dts-qcom-monaco-draco-evk-Enable-PCIe-Support.patch \
    file://patches/0005-FROMLIST-PCI-qcom-Honor-IOMMU-provider-s-iommu-cells.patch \
    file://patches/0006-arm64-dts-qcom-Enable-Monaco-Draco-connectivity.patch \
    file://patches/0007-FROMLIST-net-qrtr-Allow-the-host-QRTR-to-assign-a-un.patch \
    file://patches/0008-FROMLIST-net-qrtr-Assign-unique-node-id-for-MHI-endp.patch \
    file://patches/0009-FROMLIST-wifi-ath11k-Connect-to-the-QMI-server-belon.patch \
    file://patches/0010-FROMLIST-wifi-ath12k-Connect-to-the-QMI-server-belon.patch \
    file://patches/0011-arm64-dts-qcom-monaco-draco-evk-Enable-native-Displa.patch \
    file://patches/0012-arm64-dts-qcom-monaco-draco-evk-Add-I2C-aliases.patch \
    file://patches/0013-arm64-dts-qcom-monaco-draco-evk-Enable-DSI-to-HDMI-o.patch \
    file://patches/0014-arm64-dts-qcom-monaco-draco-evk-Add-DLC0697-DSI-pane.patch \
    file://patches/0015-arm64-dts-qcom-monaco-draco-evk-Add-Raspberry-Pi-7-D.patch \
    file://patches/0016-arm64-dts-qcom-monaco-draco-evk-Add-Waveshare-8-DSI-.patch \
    file://patches/0017-arm64-dts-qcom-monaco-draco-evk-Add-LVDS-AUO-G133HAN.patch \
    file://patches/0018-FROMLIST-arm64-defconfig-Enable-RPi-touchscreen-V2-r.patch \
    file://patches/0019-FROMLIST-arm64-defconfig-Enable-Waveshare-DSI-panel-.patch \
    file://patches/0020-QCLINUX-arm64-dts-qcom-monaco-Add-camera-sensor-node.patch \
    file://patches/0021-QCLINUX-prune.config-Enable-the-Goodix-touchscreen-d.patch \
    file://patches/0022-arm64-dts-qcom-monaco-evk-Enable-TPM-ST33.patch \
    file://patches/0023-arm64-dts-qcom-monaco-draco-ac-evk-som-Enable-Adreno.patch \
    file://patches/0024-arm64-dts-qcom-monaco-draco-evk-add-sound-support-fo.patch \
    file://patches/0025-ASoC-qcom-sc8280xp-Add-routes-and-controls-for-audio.patch \
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
