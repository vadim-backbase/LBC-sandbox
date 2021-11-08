function loginInit() {
  if (createDropdownHandler) {
    createDropdownHandler('kc-locale-dropdown-menu', 'kc-current-locale-link');
  }

  const deviceForm = document.getElementById('bb-device-list-form');
  if (deviceForm) {
    setupDeviceForm();
  }
}
