
export const useSnack = () => {
  const snackbar = useSnackbar();

  function snackbarSuccess(text: string) {
    snackbar.add({
      type: 'success',
      text: text
    });
  }
  function snackbarError(text: string, duration?: number) {
    snackbar.add({
      type: 'error',
      text: text,
      duration: duration
    });
  }
  function snackbarInfo(text: string, duration: number){
    snackbar.add({
      type: 'info',
      text: text,
      duration: duration
    });
  }
  function clearSnackbars(){
    snackbar.clear();
  }
  return { snackbarSuccess, snackbarError, snackbarInfo, clearSnackbars };
}
