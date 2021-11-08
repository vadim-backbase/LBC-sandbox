function setupDeviceForm() {
  let selectedDevice;
  let devices = [];
  
  function getDeviceFromElement(device) {
    const deviceId = device.getAttribute('value');
    const displayName = device.textContent.trim();
    return { deviceId, displayName };
  }
  
  function updateDeviceOnDom() {
    const deviceForm = document.getElementById('bb-device-list-form');
    const selectedDeviceElement = document.getElementById('bb-selected-device');
    selectedDeviceElement.textContent = selectedDevice.displayName;
    deviceForm.device.value = selectedDevice.deviceId;
  } 
  
  function setDevice(device) {
      selectedDevice = getDeviceFromElement(device);
      updateDeviceOnDom();
  }
  
  function deviceFormInit() {
    const deviceDropdownMenuId = 'bb-device-dropdown-menu';
    if (createDropdownHandler) {
      createDropdownHandler(deviceDropdownMenuId, 'bb-device-dropdown-button', true);
    }
  
    // Create devices list
    const listElement = document.getElementById(deviceDropdownMenuId);
    for (let device of listElement.children) {
        devices.push(getDeviceFromElement(device));
        device.addEventListener('keyup', (event) => {
            if (event.key === 'Enter') {
                setDevice(device);
            }
        });
    }
  
    // Set current device to first from list
    if (devices) {
        selectedDevice = devices[0];
        updateDeviceOnDom();
    }
  }

  deviceFormInit();
}